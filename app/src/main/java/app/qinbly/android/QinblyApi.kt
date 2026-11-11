package app.qinbly.android

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Cliente HTTP mínimo para a API QINBLY. Nunca usa senha local como autenticação. */
object QinblyApi {
    @Volatile private var applicationContext: Context? = null
    private val refreshLock = Any()

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    /** Token da sessão atual. Leituras públicas o enviam sempre, para o servidor devolver curtidas/seguindo do usuário e aplicar bloqueios. */
    private fun storedAccessToken(): String? =
        applicationContext?.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE)?.getString("token", null)?.takeIf { it.isNotBlank() }

    data class AuthResult(val username: String, val token: String, val refreshToken: String)

    fun authenticateWithGoogle(idToken: String): AuthResult {
        require(idToken.isNotBlank() && idToken.length <= 10000) { "Credencial Google inválida." }
        val response = request("POST", "/api/auth/google", JSONObject().put("idToken", idToken))
        val user = response.optJSONObject("user") ?: throw IllegalStateException("Resposta inválida do servidor.")
        val token = response.optString("token")
        val name = user.optString("username")
        if (token.isBlank() || name.isBlank()) throw IllegalStateException("O servidor não retornou uma sessão válida.")
        return AuthResult(name, token, response.optString("refreshToken"))
    }

    fun authenticate(register: Boolean, username: String, email: String, password: String): AuthResult {
        val path = if (register) "/api/auth/register" else "/api/auth/login"
        val payload = JSONObject().put("email", email.trim().lowercase()).put("password", password)
        if (register) payload.put("username", username.trim().lowercase())
        val response = request("POST", path, payload)
        val user = response.optJSONObject("user") ?: throw IllegalStateException("Resposta inválida do servidor.")
        val token = response.optString("token")
        val name = user.optString("username")
        if (token.isBlank() || name.isBlank()) throw IllegalStateException("O servidor não retornou uma sessão válida.")
        return AuthResult(name, token, response.optString("refreshToken"))
    }

    fun refreshSession(refreshToken: String): JSONObject {
        require(refreshToken.isNotBlank()) { "Sua sessão precisa ser renovada. Entre novamente na conta uma vez." }
        return request("POST", "/api/auth/refresh", JSONObject().put("refreshToken", refreshToken))
    }

    fun updateProfile(token: String, username: String, displayName: String, bio: String): JSONObject =
        request("PATCH", "/api/users/me",
            JSONObject().put("username", username.trim().lowercase())
                .put("displayName", displayName.trim()).put("bio", bio.trim()), token)

    /** true se o @usuário está livre (a rota é pública). */
    fun isUsernameAvailable(username: String): Boolean =
        request("GET", "/api/users/username-available?u=" + java.net.URLEncoder.encode(username.trim().lowercase(), "UTF-8")).optBoolean("available", false)

    fun getNavigationAppearance(): JSONObject = request("GET", "/api/ui/navigation")

    fun getMyProfile(token: String): JSONObject = request("GET", "/api/auth/me", null, token)

    fun exportMyData(token: String): JSONObject = request("GET", "/api/users/me/export", null, token)

    fun uploadProfileImage(context: Context, token: String, uri: Uri, category: String): String {
        require(category == "avatar" || category == "cover" || category == "cover_original") { "Tipo de imagem de perfil inválido." }
        val contentType = (context.contentResolver.getType(uri) ?: "image/jpeg").lowercase()
        require(contentType in setOf("image/jpeg", "image/png", "image/webp")) { "Escolha uma imagem JPG, PNG ou WebP." }
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(64 * 1024)
            var total = 0
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                require(total <= 25 * 1024 * 1024) { "A imagem deve ter até 25 MB." }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        } ?: throw IllegalStateException("Não foi possível ler a imagem selecionada.")
        require(bytes.isNotEmpty()) { "A imagem selecionada está vazia." }
        val prefs = context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE)
        val storedRefreshToken = prefs.getString("refresh_token", null).orEmpty()
        var activeToken = token
        if (storedRefreshToken.isBlank()) {
            // Nunca tenta upload com access token antigo sem credencial de renovação.
            prefs.edit().remove("token").apply()
            try {
                val currentUsername = prefs.getString("username", "").orEmpty()
                val accounts = org.json.JSONArray(prefs.getString("qinbly_accounts", "[]") ?: "[]")
                for (i in 0 until accounts.length()) {
                    val account = accounts.optJSONObject(i) ?: continue
                    if (account.optString("username").equals(currentUsername, true)) {
                        account.remove("token")
                        account.remove("refreshToken")
                    }
                }
                prefs.edit().putString("qinbly_accounts", accounts.toString()).apply()
            } catch (_: Exception) { }
            throw IllegalStateException("Esta conta não tem um token de renovação válido. Entre novamente com o Google para gerar uma sessão nova e depois salve a foto.")
        }
        if (storedRefreshToken.isNotBlank()) {
            // Aceita tanto o formato da API QINBLY (token/refreshToken) quanto
            // o formato padrão do Supabase Auth (access_token/refresh_token).
            val refreshed = try {
                refreshSession(storedRefreshToken)
            } catch (e: Exception) {
                // O refresh token foi recusado: descarta as credenciais antigas desta conta
                // para que o próximo login gere uma sessão nova, em vez de reutilizar JWT vencido.
                prefs.edit().remove("token").remove("refresh_token").apply()
                try {
                    val currentUsername = prefs.getString("username", "").orEmpty()
                    val accounts = org.json.JSONArray(prefs.getString("qinbly_accounts", "[]") ?: "[]")
                    for (i in 0 until accounts.length()) {
                        val account = accounts.optJSONObject(i) ?: continue
                        if (account.optString("username").equals(currentUsername, true)) {
                            account.remove("token")
                            account.remove("refreshToken")
                        }
                    }
                    prefs.edit().putString("qinbly_accounts", accounts.toString()).apply()
                } catch (_: Exception) { }
                throw IllegalStateException(
                    "A sessão antiga foi descartada porque o servidor recusou a renovação. Entre novamente com o Google para criar uma sessão nova antes de salvar a foto."
                )
            }
            activeToken = refreshed.optString("token")
                .ifBlank { refreshed.optString("access_token") }
                .ifBlank { refreshed.optString("accessToken") }
            if (activeToken.isBlank()) {
                throw IllegalStateException("O servidor não retornou um token válido ao renovar a sessão.")
            }
            val nextRefreshToken = refreshed.optString("refreshToken")
                .ifBlank { refreshed.optString("refresh_token") }
                .ifBlank { storedRefreshToken }
            prefs.edit().putString("token", activeToken).putString("refresh_token", nextRefreshToken).apply()
            try {
                val currentUsername = prefs.getString("username", "").orEmpty()
                val accounts = org.json.JSONArray(prefs.getString("qinbly_accounts", "[]") ?: "[]")
                for (i in 0 until accounts.length()) {
                    val account = accounts.optJSONObject(i) ?: continue
                    if (account.optString("username").equals(currentUsername, true)) {
                        account.put("token", activeToken).put("refreshToken", nextRefreshToken)
                    }
                }
                prefs.edit().putString("qinbly_accounts", accounts.toString()).apply()
            } catch (_: Exception) { }
        }
        val fileName = "profile-${category}-${System.currentTimeMillis()}." + when (contentType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val upload = request("POST", "/api/files/upload-url",
            JSONObject().put("fileName", fileName).put("contentType", contentType)
                .put("sizeBytes", bytes.size).put("category", category), activeToken)
        val file = upload.optJSONObject("file") ?: throw IllegalStateException("O servidor não reservou a imagem.")
        val fileId = file.optString("id")
        val uploadUrl = upload.optString("uploadUrl")
        if (fileId.isBlank() || uploadUrl.isBlank()) throw IllegalStateException("Resposta de upload inválida.")
        val connection = (URL(uploadUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "PUT"
            connectTimeout = 20000
            readTimeout = 60000
            doOutput = true
            setRequestProperty("Content-Type", contentType)
            setRequestProperty("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
            setRequestProperty("Authorization", "Bearer $activeToken")
            setFixedLengthStreamingMode(bytes.size)
        }
        try {
            connection.outputStream.use { it.write(bytes) }
            val status = connection.responseCode
            if (status !in 200..299) {
                val detail = try {
                    (if (status >= 400) connection.errorStream else connection.inputStream)
                        ?.bufferedReader()?.use { it.readText() }.orEmpty()
                } catch (_: Exception) { "" }
                throw IllegalStateException("Upload da imagem recusado (HTTP $status): " + detail.ifBlank { connection.responseMessage ?: "sem detalhes do servidor" })
            }
        } finally {
            connection.disconnect()
        }
        request("POST", "/api/files/$fileId/complete", JSONObject(), activeToken)
        request("PATCH", "/api/users/me/$category", JSONObject().put("fileId", fileId), activeToken)
        val user = request("GET", "/api/auth/me", null, activeToken).optJSONObject("user")
        return user?.optString(when (category) { "cover" -> "coverUrl"; "cover_original" -> "originalCoverUrl"; else -> "avatarUrl" }).orEmpty()
    }

    data class PostSummary(
        val id: String, val caption: String, val mediaUrl: String, val mediaType: String,
        val username: String, val displayName: String, val likes: Int, val comments: Int,
        val avatarUrl: String = "", val likedByMe: Boolean = false, val isFollowing: Boolean = false,
        val createdAt: String = "", val mediaUrls: List<String> = emptyList(), val pollOptions: List<String> = emptyList(),
        val pollVotes: List<Int> = emptyList(), val myPollVote: Int = -1,
        val audioId: String = "", val audioTitle: String = "", val audioArtist: String = "",
        val authorId: String = "",
        val savedByMe: Boolean = false, val repostedByMe: Boolean = false, val reposts: Int = 0,
        val pinned: Boolean = false, val archived: Boolean = false,
        val taggedUsers: List<String> = emptyList(),
        val verified: Boolean = false
    )

    /** Converte uma publicação do JSON da API. `fallback` (dono do perfil) preenche o autor quando a lista não o traz. */
    private fun parsePost(post: JSONObject, fallback: JSONObject? = null): PostSummary {
        val author = post.optJSONObject("author") ?: JSONObject()
        return PostSummary(
        verified = author.optBoolean("verified", fallback?.optBoolean("verified") ?: false),
        authorId = author.optString("id").ifBlank { fallback?.optString("id").orEmpty() },
        id = post.optString("id"),
        caption = post.optString("caption"),
        mediaUrl = post.optString("mediaUrl"),
        mediaType = post.optString("mediaType"),
        username = author.optString("username").ifBlank { fallback?.optString("username").orEmpty() },
        displayName = author.optString("displayName", author.optString("display_name")).ifBlank { fallback?.optString("displayName").orEmpty() },
        likes = post.optInt("likes"),
        comments = post.optInt("comments"),
        avatarUrl = author.optString("avatarUrl", author.optString("avatar_url")).ifBlank { fallback?.optString("avatarUrl").orEmpty() },
        likedByMe = post.optBoolean("likedByMe"),
        savedByMe = post.optBoolean("savedByMe"),
        repostedByMe = post.optBoolean("repostedByMe"),
        reposts = post.optInt("reposts"),
        pinned = post.optBoolean("pinned"),
        archived = post.optBoolean("archived"),
        isFollowing = post.optBoolean("isFollowing"),
        createdAt = post.optString("createdAt", post.optString("created_at")),
        audioId = post.optString("audioId", post.optString("audio_id")).ifBlank { post.optJSONObject("audio")?.optString("id").orEmpty() },
        audioTitle = post.optString("audioTitle", post.optString("audio_title")).ifBlank { post.optJSONObject("audio")?.optString("title").orEmpty() },
        audioArtist = post.optString("audioArtist", post.optString("audio_artist")).ifBlank { post.optJSONObject("audio")?.optString("artist").orEmpty() },
        mediaUrls = post.optJSONArray("mediaUrls")?.let { a -> (0 until a.length()).map { a.optString(it) } } ?: emptyList(),
        pollOptions = post.optJSONArray("pollOptions")?.let { a -> (0 until a.length()).map { a.optString(it) } } ?: emptyList(),
        pollVotes = post.optJSONArray("pollOptions")?.let { a -> List(a.length()) { i ->
            val counts = post.optJSONArray("pollVotes") ?: org.json.JSONArray()
            (0 until counts.length()).mapNotNull { counts.optJSONObject(it) }.firstOrNull { it.optInt("optionIndex") == i }?.optInt("votes") ?: 0
        } } ?: emptyList(),
        myPollVote = post.optInt("myPollVote", -1),
        taggedUsers = post.optJSONArray("taggedUsers")?.let { a -> (0 until a.length()).map { a.optString(it) }.filter { it.isNotBlank() } } ?: emptyList()
            )
    }

    /** Publicações de um array JSON (feed ou perfil). */
    fun parsePosts(array: org.json.JSONArray?, fallback: JSONObject? = null): List<PostSummary> {
        val posts = array ?: org.json.JSONArray()
        return (0 until posts.length()).mapNotNull { index -> posts.optJSONObject(index)?.let { parsePost(it, fallback) } }
    }

    fun getPosts(token: String? = null): List<PostSummary> {
        val response = request("GET", "/api/posts?limit=30", null, token ?: storedAccessToken())
        val result = parsePosts(response.optJSONArray("posts"))
        applicationContext?.let { cachePosts(it, response.optJSONArray("posts")) }
        return result
    }

    /** Cache local para abrir o feed na hora (e offline); o servidor continua sendo a fonte oficial. Guarda o JSON completo. */
    fun getCachedPosts(context: Context): List<PostSummary> {
        return try {
            val raw = context.getSharedPreferences("qbly_local_cache", Context.MODE_PRIVATE)
                .getString("feed_posts_v2", null) ?: return emptyList()
            parsePosts(org.json.JSONArray(raw))
        } catch (_: Exception) { emptyList() }
    }

    private fun cachePosts(context: Context, posts: org.json.JSONArray?) {
        try {
            if (posts == null) return
            context.getSharedPreferences("qbly_local_cache", Context.MODE_PRIVATE)
                .edit().putString("feed_posts_v2", posts.toString()).apply()
        } catch (_: Exception) { /* Cache failure must never block the feed. */ }
    }

    fun createTextPost(token: String, caption: String, visibility: String = "public", tagged: List<String> = emptyList()): JSONObject =
        request("POST", "/api/posts", JSONObject().put("caption", caption.trim()).put("mediaType", "text").put("visibility", visibility).put("taggedUsernames", org.json.JSONArray(tagged)), token)

    fun createPollPost(token: String, question: String, options: List<String>, durationHours: Int = 24, multipleAnswers: Boolean = false, showResultsAfterVote: Boolean = true): JSONObject =
        request("POST", "/api/posts", JSONObject().put("caption", question.trim())
            .put("mediaType", "poll").put("pollOptions", org.json.JSONArray(options))
            .put("durationHours", durationHours).put("multipleAnswers", multipleAnswers)
            .put("showResultsAfterVote", showResultsAfterVote), token)

    fun votePoll(token: String, postId: String, optionIndex: Int): JSONObject =
        request("POST", "/api/posts/${postId}/vote", JSONObject().put("optionIndex", optionIndex), token)

    fun createMediaPost(token: String, caption: String, fileId: String, mediaType: String, visibility: String = "public", tagged: List<String> = emptyList()): JSONObject =
        request("POST", "/api/posts", JSONObject().put("caption", caption.trim())
            .put("mediaFileId", fileId).put("mediaType", mediaType).put("visibility", visibility).put("taggedUsernames", org.json.JSONArray(tagged)), token)

    fun createPhotoAlbum(token: String, caption: String, fileIds: List<String>, visibility: String = "public", tagged: List<String> = emptyList()): JSONObject {
        require(fileIds.size in 1..10) { "Selecione de 1 a 10 fotos." }
        return request("POST", "/api/posts", JSONObject().put("caption", caption.trim())
            .put("mediaType", "image").put("mediaFileId", fileIds.first())
            .put("mediaFileIds", org.json.JSONArray(fileIds)).put("visibility", visibility).put("taggedUsernames", org.json.JSONArray(tagged)), token)
    }

    fun uploadPostMedia(context: Context, token: String, uri: Uri, fileName: String, contentType: String, sizeBytes: Long, category: String = "post_media"): String {
        require(sizeBytes in 1..(100L * 1024L * 1024L)) { "O arquivo está vazio ou ultrapassa 100 MB." }
        val requestBody = JSONObject().put("fileName", fileName).put("contentType", contentType)
            .put("sizeBytes", sizeBytes).put("category", category)
        val upload = request("POST", "/api/files/upload-url", requestBody, token)
        val file = upload.optJSONObject("file") ?: throw IllegalStateException("O servidor não reservou o arquivo.")
        val fileId = file.optString("id")
        val uploadUrl = upload.optString("uploadUrl")
        if (fileId.isBlank() || uploadUrl.isBlank()) throw IllegalStateException("Resposta de upload inválida.")
        val connection = (URL(uploadUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "PUT"
            connectTimeout = 20000
            readTimeout = 60000
            doOutput = true
            setRequestProperty("Content-Type", contentType)
            setRequestProperty("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
            setRequestProperty("Authorization", "Bearer $token")
            setFixedLengthStreamingMode(sizeBytes)
        }
        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Não foi possível ler o arquivo selecionado.")
            input.use { source -> connection.outputStream.use { output -> source.copyTo(output, 64 * 1024) } }
            val status = connection.responseCode
            if (status !in 200..299) throw IllegalStateException("Falha no envio da mídia (HTTP $status).")
        } finally {
            connection.disconnect()
        }
        request("POST", "/api/files/" + fileId + "/complete", JSONObject(), token)
        return fileId
    }

    /** Edita a legenda de uma publicação própria. */
    fun editPost(token: String, postId: String, caption: String): String {
        val r = request("PATCH", "/api/posts/" + postId, JSONObject().put("caption", caption), token)
        return r.optString("caption", caption)
    }

    /** Edição completa: legenda e, opcionalmente, troca ou remoção da mídia. Retorna a publicação atualizada. */
    fun editPostFull(token: String, postId: String, caption: String, newMediaFileId: String? = null, newMediaType: String? = null, removeMedia: Boolean = false): PostSummary {
        val body = JSONObject().put("caption", caption)
        if (!newMediaFileId.isNullOrBlank()) body.put("mediaFileId", newMediaFileId).put("mediaType", newMediaType ?: "image")
        else if (removeMedia) body.put("removeMedia", true)
        request("PATCH", "/api/posts/" + postId, body, token)
        return getPost(postId)
    }

    fun deletePost(token: String, postId: String) {
        request("DELETE", "/api/posts/" + postId, JSONObject(), token)
    }

    // ---- Comunidade: bloquear, denunciar, excluir conta ----
    data class BlockedUser(val id: String, val username: String, val displayName: String)

    /** Alterna o bloqueio. Retorna true se o usuário ficou bloqueado, false se foi desbloqueado. */
    fun toggleBlock(token: String, userId: String): Boolean =
        request("POST", "/api/users/" + userId + "/block", JSONObject(), token).optBoolean("blocked")

    private fun userIdOf(username: String, token: String): String {
        val profile = getPublicProfile(username, token)
        val id = (profile.optJSONObject("user") ?: profile).optString("id")
        if (id.isBlank()) throw IllegalStateException("Não foi possível localizar este perfil.")
        return id
    }

    fun blockByUsername(token: String, username: String): Boolean = toggleBlock(token, userIdOf(username, token))

    fun getBlockedUsers(token: String): List<BlockedUser> {
        val blocks = request("GET", "/api/users/me/blocks", null, token).optJSONArray("blocks") ?: org.json.JSONArray()
        return (0 until blocks.length()).mapNotNull { index ->
            val b = blocks.optJSONObject(index) ?: return@mapNotNull null
            BlockedUser(b.optString("id"), b.optString("username"), b.optString("displayName"))
        }
    }

    /** targetType: post, comment, user ou message. reason: spam, harassment, hate, violence, sexual, minor_safety, self_harm, illegal ou other. */
    fun reportContent(token: String, targetType: String, targetId: String, reason: String) {
        request("POST", "/api/reports", JSONObject().put("targetType", targetType).put("targetId", targetId).put("reason", reason), token)
    }

    fun reportUserByUsername(token: String, username: String, reason: String) =
        reportContent(token, "user", userIdOf(username, token), reason)

    fun editComment(token: String, commentId: String, body: String) {
        request("PATCH", "/api/comments/" + commentId, JSONObject().put("body", body), token)
    }

    fun deleteComment(token: String, commentId: String) {
        request("DELETE", "/api/comments/" + commentId, JSONObject(), token)
    }

    /** Exclui a conta e todos os dados. confirmUsername precisa ser o nome de usuário atual. */
    fun deleteAccount(token: String, confirmUsername: String) {
        request("POST", "/api/users/me/delete", JSONObject().put("confirmUsername", confirmUsername), token)
    }

    fun toggleLike(token: String, postId: String): Boolean =
        request("POST", "/api/posts/" + postId + "/like", JSONObject(), token).optBoolean("liked")

    /** Hashtags mais usadas nas publicações recentes. */
    fun getTrendingTags(): List<Pair<String, Int>> {
        val arr = request("GET", "/api/hashtags/trending", null, storedAccessToken()).optJSONArray("tags") ?: return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val tag = o.optString("tag")
            if (tag.isBlank()) null else tag to o.optInt("posts")
        }
    }

    /** Publicações públicas que usam a hashtag. */
    fun getTagPosts(tag: String): List<PostSummary> {
        val clean = java.net.URLEncoder.encode(tag.removePrefix("#"), "UTF-8")
        return parsePosts(request("GET", "/api/hashtags/$clean", null, storedAccessToken()).optJSONArray("posts"))
    }

    /** Pessoas que a pessoa ainda não segue. */
    fun getSuggestedUsers(): List<UserSummary> {
        val arr = request("GET", "/api/users/suggested", null, storedAccessToken()).optJSONArray("users") ?: return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val u = arr.optJSONObject(i) ?: return@mapNotNull null
            val name = u.optString("username").trim()
            if (name.isBlank()) return@mapNotNull null
            UserSummary(
                id = u.optString("id").ifBlank { name }, username = name,
                displayName = u.optString("displayName").ifBlank { name }, bio = u.optString("bio"),
                avatarUrl = u.optString("avatarUrl"), verified = u.optBoolean("verified"),
                isFollowing = u.optBoolean("isFollowing")
            )
        }
    }

    /** Pede o e-mail de redefinição de senha. */
    fun forgotPassword(email: String): String {
        val r = request("POST", "/api/auth/forgot", JSONObject().put("email", email.trim().lowercase()))
        return r.optString("message", "Se esse e-mail tiver uma conta, enviamos o link para criar uma nova senha.")
    }

    data class CommunitySummary(
        val id: String, val name: String, val description: String, val emoji: String,
        val ownerUsername: String, val members: Int, val joined: Boolean, val isOwner: Boolean
    )
    data class CommunityPost(
        val id: String, val body: String, val createdAt: String,
        val username: String, val displayName: String, val avatarUrl: String, val mine: Boolean,
        val imageUrl: String = "", val audioUrl: String = "", val audioSeconds: Int = 0,
        val fileUrl: String = "", val fileName: String = "", val poll: ChatPoll? = null,
        val stickerUrl: String = ""
    )

    private fun parseCommunity(o: JSONObject) = CommunitySummary(
        id = o.optString("id"), name = o.optString("name"), description = o.optString("description"),
        emoji = o.optString("emoji", "👥").ifBlank { "👥" }, ownerUsername = o.optString("ownerUsername"),
        members = o.optInt("members"), joined = o.optBoolean("joined"), isOwner = o.optBoolean("isOwner")
    )

    /** filter: "mine", "trending" ou "discover". */
    fun getCommunities(filter: String, token: String?): List<CommunitySummary> {
        val arr = request("GET", "/api/communities?filter=" + filter, null, token).optJSONArray("communities") ?: return emptyList()
        return (0 until arr.length()).mapNotNull { arr.optJSONObject(it)?.let(::parseCommunity) }
    }

    fun getCommunity(id: String, token: String?): Pair<CommunitySummary, List<CommunityPost>> {
        val r = request("GET", "/api/communities/" + id, null, token)
        val community = parseCommunity(r.optJSONObject("community") ?: throw IllegalStateException("Comunidade não encontrada."))
        val arr = r.optJSONArray("posts")
        val posts = if (arr == null) emptyList() else (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val a = o.optJSONObject("author")
            CommunityPost(o.optString("id"), o.optString("body"), o.optString("createdAt"),
                a?.optString("username").orEmpty(), a?.optString("displayName").orEmpty(), a?.optString("avatarUrl").orEmpty(), o.optBoolean("mine"),
                o.optString("imageUrl"), o.optString("audioUrl"), o.optInt("audioSeconds"),
                o.optString("fileUrl"), o.optString("fileName"), parseChatPoll(o.optJSONObject("poll")), o.optString("stickerUrl"))
        }
        return community to posts
    }

    fun createCommunity(token: String, name: String, description: String, emoji: String): CommunitySummary {
        val r = request("POST", "/api/communities", JSONObject().put("name", name).put("description", description).put("emoji", emoji), token)
        return parseCommunity(r.optJSONObject("community") ?: throw IllegalStateException("Não foi possível criar a comunidade."))
    }

    /** Entra/sai. Retorna (participando, total de membros). */
    fun toggleCommunityJoin(token: String, id: String): Pair<Boolean, Int> {
        val r = request("POST", "/api/communities/" + id + "/join", JSONObject(), token)
        return r.optBoolean("joined") to r.optInt("members")
    }

    fun addCommunityPost(token: String, id: String, body: String) {
        request("POST", "/api/communities/" + id + "/posts", JSONObject().put("body", body), token)
    }

    /** Sobe um documento (PDF, Word, Excel, PowerPoint, texto, CSV, ZIP) e devolve a marca "qinbly:file:<arquivo>:<nome>". */
    private fun uploadChatFile(context: Context, token: String, uri: Uri): String {
        val resolver = context.contentResolver
        val type = (resolver.getType(uri) ?: "").lowercase()
        var name = "documento"
        var size = -1L
        resolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME, android.provider.OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                c.getString(0)?.let { name = it }
                if (!c.isNull(1)) size = c.getLong(1)
            }
        }
        if (size <= 0) size = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        require(size > 0) { "Não foi possível ler o documento." }
        require(size <= 25L * 1024 * 1024) { "O documento passa de 25 MB." }
        val allowed = setOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", "text/plain", "text/csv", "application/zip", "application/x-zip-compressed")
        require(type in allowed) { "Esse tipo de documento não é aceito. Use PDF, Word, Excel, PowerPoint, texto, CSV ou ZIP." }
        val fileId = uploadPostMedia(context, token, uri, name, type, size, "message_file")
        return "qinbly:file:" + fileId + ":" + java.net.URLEncoder.encode(name.take(100), "UTF-8")
    }

    fun sendFileMessage(context: Context, token: String, username: String, uri: Uri): JSONObject =
        sendMessage(token, username, uploadChatFile(context, token, uri))

    fun sendCommunityFile(context: Context, token: String, communityId: String, uri: Uri) {
        addCommunityPost(token, communityId, uploadChatFile(context, token, uri))
    }

    /** Cria a enquete no servidor e devolve a marca "qinbly:poll:<id>" para mandar como mensagem. */
    private fun createChatPollToken(token: String, question: String, options: List<String>): String {
        val r = request("POST", "/api/chat-polls", JSONObject().put("question", question.trim()).put("options", org.json.JSONArray(options.map { it.trim() })), token)
        val id = r.optString("id")
        require(id.isNotBlank()) { "Não foi possível criar a enquete." }
        return "qinbly:poll:" + id
    }

    fun sendPollMessage(token: String, username: String, question: String, options: List<String>): JSONObject =
        sendMessage(token, username, createChatPollToken(token, question, options))

    fun sendCommunityPoll(token: String, communityId: String, question: String, options: List<String>) {
        addCommunityPost(token, communityId, createChatPollToken(token, question, options))
    }

    fun voteChatPoll(token: String, pollId: String, optionIndex: Int): ChatPoll {
        val r = request("POST", "/api/chat-polls/" + pollId + "/vote", JSONObject().put("optionIndex", optionIndex), token)
        val counts = r.optJSONArray("votes")?.let { a -> (0 until a.length()).map { a.optInt(it) } } ?: emptyList()
        return ChatPoll(pollId, "", emptyList(), counts, r.optInt("myVote", optionIndex))
    }

    // ───────── Figurinhas próprias ─────────
    data class CustomSticker(val id: String, val path: String, val url: String)

    private fun parseSticker(o: JSONObject) = CustomSticker(o.optString("id"), o.optString("path"), o.optString("url"))

    fun getMyStickers(token: String): List<CustomSticker> {
        val arr = request("GET", "/api/stickers", null, token).optJSONArray("stickers") ?: return emptyList()
        return (0 until arr.length()).mapNotNull { arr.optJSONObject(it)?.let(::parseSticker) }
    }

    /** Sobe a imagem da figurinha (PNG/WebP, até 2 MB) e registra na sua coleção. */
    fun createSticker(context: Context, token: String, file: java.io.File, contentType: String): CustomSticker {
        val fileId = uploadPostMedia(context, token, Uri.fromFile(file), file.name, contentType, file.length(), "sticker")
        val r = request("POST", "/api/stickers", JSONObject().put("fileId", fileId), token)
        return parseSticker(r.optJSONObject("sticker") ?: throw IllegalStateException("Não foi possível salvar a figurinha."))
    }

    fun deleteSticker(token: String, id: String) {
        request("DELETE", "/api/stickers/" + id, null, token)
    }

    fun sendStickerMessage(token: String, username: String, sticker: CustomSticker): JSONObject =
        sendMessage(token, username, "qinbly:sticker:" + sticker.path)

    fun sendCommunitySticker(token: String, communityId: String, sticker: CustomSticker) {
        addCommunityPost(token, communityId, "qinbly:sticker:" + sticker.path)
    }

    // ───────── Stories (24 h) e notas ─────────
    data class StoryItem(val id: String, val mediaUrl: String, val mediaType: String, val caption: String, val createdAt: String, val seen: Boolean, val views: Int)
    data class TrayEntry(val username: String, val displayName: String, val avatarUrl: String, val mine: Boolean, val note: String, val stories: List<StoryItem>, val hasUnseen: Boolean)
    data class StoryViewer(val username: String, val displayName: String, val avatarUrl: String, val viewedAt: String)

    /** Bandeja de stories e notas: você primeiro, depois quem você segue (com story novo antes). */
    fun getStoryTray(token: String): List<TrayEntry> {
        val arr = request("GET", "/api/stories", null, token).optJSONArray("tray") ?: return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val items = o.optJSONArray("stories")?.let { a ->
                (0 until a.length()).mapNotNull { j ->
                    a.optJSONObject(j)?.let { StoryItem(it.optString("id"), it.optString("mediaUrl"), it.optString("mediaType"), it.optString("caption"), it.optString("createdAt"), it.optBoolean("seen"), it.optInt("views")) }
                }
            } ?: emptyList()
            TrayEntry(o.optString("username"), o.optString("displayName"), o.optString("avatarUrl"), o.optBoolean("mine"),
                o.optJSONObject("note")?.optString("body").orEmpty(), items, o.optBoolean("hasUnseen"))
        }
    }

    /** Publica um story de foto ou vídeo da galeria/câmera; sobe como categoria `story_media`. */
    fun createStory(context: Context, token: String, uri: Uri, caption: String) {
        val resolver = context.contentResolver
        val type = (resolver.getType(uri) ?: "image/jpeg").lowercase()
        val isVideo = type.startsWith("video/")
        require(isVideo || type in setOf("image/jpeg", "image/png", "image/webp")) { "Esse tipo de arquivo não é aceito. Use foto (JPG, PNG, WebP) ou vídeo." }
        val size = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        require(size > 0) { "Não foi possível ler o arquivo." }
        require(size <= (if (isVideo) 100L else 25L) * 1024 * 1024) { if (isVideo) "O vídeo passa de 100 MB." else "A foto passa de 25 MB." }
        val ext = when (type) { "image/png" -> "png"; "image/webp" -> "webp"; "video/quicktime" -> "mov"; "video/webm" -> "webm"; "video/mp4" -> "mp4"; else -> if (isVideo) "mp4" else "jpg" }
        val fileId = uploadPostMedia(context, token, uri, "story.$ext", type, size, "story_media")
        request("POST", "/api/stories", JSONObject().put("mediaFileId", fileId).put("mediaType", if (isVideo) "video" else "image").put("caption", caption.trim()), token)
    }

    fun deleteStory(token: String, id: String) { request("DELETE", "/api/stories/" + id, null, token) }

    fun markStoryViewed(token: String, id: String) { request("POST", "/api/stories/" + id + "/view", JSONObject(), token) }

    fun getStoryViewers(token: String, id: String): List<StoryViewer> {
        val arr = request("GET", "/api/stories/" + id + "/views", null, token).optJSONArray("viewers") ?: return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            arr.optJSONObject(i)?.let { StoryViewer(it.optString("username"), it.optString("displayName"), it.optString("avatarUrl"), it.optString("viewedAt")) }
        }
    }

    fun saveNote(token: String, text: String) { request("POST", "/api/notes", JSONObject().put("body", text.trim()), token) }

    fun deleteNote(token: String) { request("DELETE", "/api/notes", null, token) }

    // ───────── Comentários de live ─────────
    data class LiveComment(val id: String, val username: String, val displayName: String, val body: String, val createdAt: String)

    /** Comentários da live de [host]; com [after] (data ISO do último recebido) traz só os novos. */
    fun getLiveComments(token: String?, host: String, after: String = ""): List<LiveComment> {
        val query = if (after.isBlank()) "" else "?after=" + java.net.URLEncoder.encode(after, "UTF-8")
        val arr = request("GET", "/api/lives/" + java.net.URLEncoder.encode(host.removePrefix("@").lowercase(), "UTF-8") + "/comments" + query, null, token).optJSONArray("comments") ?: return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            arr.optJSONObject(i)?.let { LiveComment(it.optString("id"), it.optString("username"), it.optString("displayName"), it.optString("body"), it.optString("createdAt")) }
        }
    }

    fun sendLiveComment(token: String, host: String, text: String) {
        request("POST", "/api/lives/" + java.net.URLEncoder.encode(host.removePrefix("@").lowercase(), "UTF-8") + "/comments", JSONObject().put("body", text.trim()), token)
    }

    /** Foto na conversa da comunidade (mesma categoria `message_media` das mensagens diretas). */
    fun sendCommunityImage(context: Context, token: String, communityId: String, uri: Uri) {
        val resolver = context.contentResolver
        val type = (resolver.getType(uri) ?: "image/jpeg").lowercase()
        require(type in setOf("image/jpeg", "image/png", "image/webp", "image/gif")) { "Esse tipo de foto não é aceito. Use JPG, PNG ou WebP." }
        val size = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        require(size > 0) { "Não foi possível ler a foto." }
        val ext = when (type) { "image/png" -> "png"; "image/webp" -> "webp"; "image/gif" -> "gif"; else -> "jpg" }
        val fileId = uploadPostMedia(context, token, uri, "foto.$ext", type, size, "message_media")
        addCommunityPost(token, communityId, "qinbly:img:" + fileId)
    }

    fun sendCommunityVoice(context: Context, token: String, communityId: String, file: java.io.File, seconds: Int) {
        val fileId = uploadPostMedia(context, token, Uri.fromFile(file), "voz.m4a", "audio/mp4", file.length(), "message_audio")
        addCommunityPost(token, communityId, "qinbly:audio:" + fileId + ":" + seconds.coerceAtLeast(1))
    }

    fun deleteCommunityPost(token: String, id: String, postId: String) {
        request("DELETE", "/api/communities/" + id + "/posts/" + postId, null, token)
    }

    fun deleteCommunity(token: String, id: String) {
        request("DELETE", "/api/communities/" + id, null, token)
    }

    /** Publicações que a pessoa salvou (mais recentes primeiro). */
    fun getSavedPosts(token: String): List<PostSummary> =
        parsePosts(request("GET", "/api/me/saved", null, token).optJSONArray("posts"))

    /** Abre uma publicação pelo id (usado por links compartilhados). */
    fun getPost(postId: String): PostSummary {
        val response = request("GET", "/api/posts/" + postId, null, storedAccessToken())
        return parsePost(response.optJSONObject("post") ?: throw IllegalStateException("Publicação não encontrada."))
    }

    fun toggleSave(token: String, postId: String): Boolean =
        request("POST", "/api/posts/" + postId + "/save", JSONObject(), token).optBoolean("saved")

    fun toggleRepost(token: String, postId: String): Boolean =
        request("POST", "/api/posts/" + postId + "/repost", JSONObject(), token).optBoolean("reposted")

    data class CommentSummary(
        val id: String, val body: String, val username: String, val displayName: String,
        val parentId: String = "", val likes: Int = 0, val likedByMe: Boolean = false, val createdAt: String = ""
    )

    fun getComments(postId: String): List<CommentSummary> {
        val response = request("GET", "/api/posts/" + postId + "/comments", null, storedAccessToken())
        val comments = response.optJSONArray("comments") ?: org.json.JSONArray()
        return (0 until comments.length()).mapNotNull { index ->
            val comment = comments.optJSONObject(index) ?: return@mapNotNull null
            val author = comment.optJSONObject("author") ?: org.json.JSONObject()
            CommentSummary(
                comment.optString("id"), comment.optString("body"), author.optString("username"), author.optString("displayName"),
                parentId = comment.optString("parentId"), likes = comment.optInt("likes"),
                likedByMe = comment.optBoolean("likedByMe"), createdAt = comment.optString("createdAt")
            )
        }
    }

    fun addComment(token: String, postId: String, body: String, parentId: String? = null): JSONObject {
        val payload = JSONObject().put("body", body.trim())
        if (!parentId.isNullOrBlank()) payload.put("parentId", parentId)
        return request("POST", "/api/posts/" + postId + "/comments", payload, token)
    }

    /** Curtir/descurtir um comentário. Retorna true se ficou curtido. */
    fun toggleCommentLike(token: String, commentId: String): Boolean =
        request("POST", "/api/comments/" + commentId + "/like", JSONObject(), token).optBoolean("liked")

    /** Fixa/desafixa no perfil (até 3). Retorna true se ficou fixada. */
    fun togglePin(token: String, postId: String): Boolean =
        request("POST", "/api/posts/" + postId + "/pin", JSONObject(), token).optBoolean("pinned")

    /** Arquiva/desarquiva. Retorna true se ficou arquivada. */
    fun toggleArchive(token: String, postId: String): Boolean =
        request("POST", "/api/posts/" + postId + "/archive", JSONObject(), token).optBoolean("archived")

    data class InboxSummary(
        val username: String,
        val displayName: String,
        val avatarUrl: String,
        val body: String,
        val createdAt: String,
        val unreadCount: Int
    )

    fun getInbox(token: String): List<InboxSummary> {
        val response = request("GET", "/api/inbox", null, token)
        val items = response.optJSONArray("conversations") ?: org.json.JSONArray()
        return (0 until items.length()).mapNotNull { index ->
            val item = items.optJSONObject(index) ?: return@mapNotNull null
            InboxSummary(
                username = item.optString("username"),
                displayName = item.optString("displayName"),
                avatarUrl = item.optString("avatarUrl"),
                body = item.optString("body"),
                createdAt = item.optString("createdAt"),
                unreadCount = item.optInt("unreadCount")
            )
        }
    }

    /** Enquete enviada numa conversa: [votes] é a contagem por opção e [myVote] a opção escolhida por você (-1 = nenhuma). */
    data class ChatPoll(val id: String, val question: String, val options: List<String>, val votes: List<Int>, val myVote: Int)

    private fun parseChatPoll(o: JSONObject?): ChatPoll? {
        if (o == null) return null
        val opts = o.optJSONArray("options")?.let { a -> (0 until a.length()).map { a.optString(it) } } ?: return null
        val counts = o.optJSONArray("votes")?.let { a -> (0 until a.length()).map { a.optInt(it) } } ?: opts.map { 0 }
        return ChatPoll(o.optString("id"), o.optString("question"), opts, counts, o.optInt("myVote", -1))
    }

    data class MessageSummary(val id: String, val body: String, val senderId: String, val createdAt: String, val audioUrl: String = "", val audioSeconds: Int = 0, val imageUrl: String = "", val fileUrl: String = "", val fileName: String = "", val poll: ChatPoll? = null, val stickerUrl: String = "")

    fun getMessages(token: String, username: String): List<MessageSummary> {
        val name = java.net.URLEncoder.encode(username.trim().removePrefix("@").lowercase(), "UTF-8")
        val response = request("GET", "/api/messages/" + name, null, token)
        val items = response.optJSONArray("messages") ?: org.json.JSONArray()
        return (0 until items.length()).mapNotNull { index ->
            val item = items.optJSONObject(index) ?: return@mapNotNull null
            MessageSummary(item.optString("id"), item.optString("body"), item.optString("senderId"), item.optString("createdAt"), item.optString("audioUrl"), item.optInt("audioSeconds"), item.optString("imageUrl"), item.optString("fileUrl"), item.optString("fileName"), parseChatPoll(item.optJSONObject("poll")), item.optString("stickerUrl"))
        }
    }

    /** Envia uma mensagem de voz: sobe o arquivo e manda a mensagem com a marca "qinbly:audio:<arquivo>:<segundos>". */
    fun sendVoiceMessage(context: Context, token: String, username: String, file: java.io.File, seconds: Int): JSONObject {
        val fileId = uploadPostMedia(context, token, Uri.fromFile(file), "voz.m4a", "audio/mp4", file.length(), "message_audio")
        return sendMessage(token, username, "qinbly:audio:" + fileId + ":" + seconds.coerceAtLeast(1))
    }

    /** Envia uma foto numa conversa: sobe como categoria `message_media` e manda "qinbly:img:<arquivo>". */
    fun sendImageMessage(context: Context, token: String, username: String, uri: Uri): JSONObject {
        val resolver = context.contentResolver
        val type = (resolver.getType(uri) ?: "image/jpeg").lowercase()
        require(type in setOf("image/jpeg", "image/png", "image/webp", "image/gif")) { "Esse tipo de foto não é aceito. Use JPG, PNG ou WebP." }
        val size = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        require(size > 0) { "Não foi possível ler a foto." }
        val ext = when (type) { "image/png" -> "png"; "image/webp" -> "webp"; "image/gif" -> "gif"; else -> "jpg" }
        val fileId = uploadPostMedia(context, token, uri, "foto.$ext", type, size, "message_media")
        return sendMessage(token, username, "qinbly:img:" + fileId)
    }

    fun sendMessage(token: String, username: String, body: String): JSONObject {
        val name = java.net.URLEncoder.encode(username.trim().removePrefix("@").lowercase(), "UTF-8")
        return request("POST", "/api/messages/" + name, JSONObject().put("body", body.trim()), token)
    }

    data class NotificationSummary(
        val id: String,
        val kind: String,
        val entityId: String,
        val createdAt: String,
        val readAt: String,
        val actorUsername: String,
        val actorDisplayName: String,
        val actorAvatarUrl: String
    )

    fun getNotifications(token: String): List<NotificationSummary> {
        val response = request("GET", "/api/notifications", null, token)
        val items = response.optJSONArray("notifications") ?: org.json.JSONArray()
        return (0 until items.length()).mapNotNull { index ->
            val item = items.optJSONObject(index) ?: return@mapNotNull null
            val actor = item.optJSONObject("actor") ?: org.json.JSONObject()
            NotificationSummary(
                id = item.optString("id"),
                kind = item.optString("kind"),
                entityId = item.optString("entityId"),
                createdAt = item.optString("createdAt"),
                readAt = item.optString("readAt"),
                actorUsername = actor.optString("username"),
                actorDisplayName = actor.optString("displayName"),
                actorAvatarUrl = actor.optString("avatarUrl")
            )
        }
    }

    fun markNotificationRead(token: String, notificationId: String) {
        request("POST", "/api/notifications/" + java.net.URLEncoder.encode(notificationId, "UTF-8") + "/read", JSONObject(), token)
    }

    fun markNotificationsRead(token: String) {
        request("POST", "/api/notifications/read", JSONObject(), token)
    }

    data class UserSummary(
        val id: String, val username: String, val displayName: String,
        val bio: String, val avatarUrl: String, val verified: Boolean,
        val isFollowing: Boolean = false
    )

    fun toggleFollow(token: String, userId: String): Boolean {
        require(token.isNotBlank() && userId.isNotBlank()) { "Conta ou sessão inválida para seguir." }
        val response = request("POST", "/api/users/" + userId + "/follow", JSONObject(), token)
        if (!response.has("following") || response.isNull("following")) {
            throw IllegalStateException("O servidor não confirmou a alteração de seguimento.")
        }
        return response.getBoolean("following")
    }

    fun heartbeatPresence(token: String): Int =
        request("POST", "/api/presence/heartbeat", JSONObject(), token).optInt("onlineCount", -1)

    fun getPublicProfile(username: String, token: String? = null): JSONObject {
        val name = java.net.URLEncoder.encode(username.trim().removePrefix("@").lowercase(), "UTF-8")
        return request("GET", "/api/users/" + name, null, token ?: storedAccessToken())
    }

    fun getConnections(username: String, type: String): List<UserSummary> {
        require(type == "followers" || type == "following") { "Tipo de lista inválido." }
        val name = java.net.URLEncoder.encode(username.trim().removePrefix("@").lowercase(), "UTF-8")
        val sessionToken = applicationContext?.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE)?.getString("token", null)
        val response = request("GET", "/api/users/" + name + "/" + type, null, sessionToken)
        val users = response.optJSONArray("users") ?: org.json.JSONArray()
        return (0 until users.length()).mapNotNull { index ->
            val user = users.optJSONObject(index) ?: return@mapNotNull null
            UserSummary(
                id = user.optString("id"), username = user.optString("username"),
                displayName = user.optString("displayName", user.optString("display_name")),
                bio = user.optString("bio"), avatarUrl = user.optString("avatarUrl", user.optString("avatar_url")),
                verified = user.optBoolean("verified", user.optBoolean("is_verified")),
                isFollowing = user.optBoolean("isFollowing")
            )
        }
    }

    fun searchUsers(query: String, token: String? = null): List<UserSummary> {
        val normalized = query.trim().removePrefix("@").trim()
        if (normalized.length < 2) return emptyList()
        val q = java.net.URLEncoder.encode(normalized, "UTF-8")
        // Envia a sessão atual para permitir renovação automática caso o backend rejeite o token expirado.
        val response = request("GET", "/api/search?q=" + q + "&limit=30", token = token ?: storedAccessToken())
        val users = response.optJSONArray("users") ?: org.json.JSONArray()
        return (0 until users.length()).mapNotNull { index ->
            val user = users.optJSONObject(index) ?: return@mapNotNull null
            val username = user.optString("username").trim()
            if (username.isBlank()) return@mapNotNull null
            UserSummary(
                id = user.optString("id").ifBlank { username },
                username = username,
                displayName = user.optString("displayName", user.optString("display_name")).ifBlank { username },
                bio = user.optString("bio"),
                avatarUrl = user.optString("avatarUrl", user.optString("avatar_url")),
                verified = user.optBoolean("verified", user.optBoolean("is_verified")),
                isFollowing = user.optBoolean("isFollowing")
            )
        }.distinctBy { it.username.lowercase() }
    }

    private fun request(method: String, path: String, body: JSONObject? = null, token: String? = null, allowRefresh: Boolean = true): JSONObject {
        val base = BuildConfig.API_BASE_URL.trim().trimEnd('/')
        require(!base.contains("configure-your-qinbly-api.example") && (base.startsWith("https://") || base.startsWith("http://10.0.2.2"))) {
            "Backend ainda não configurado. Defina API_BASE_URL no app/build.gradle.kts com a URL HTTPS publicada."
        }
        val connection = (URL(base + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 12000
            readTimeout = 12000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
            if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
        try {
            if (body != null) connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = try { JSONObject(text) } catch (_: Exception) { JSONObject() }
            if (status == 401 && allowRefresh && !token.isNullOrBlank() &&
                path != "/api/auth/refresh" && path != "/api/auth/google" &&
                path != "/api/auth/login" && path != "/api/auth/register") {
                val refreshedToken = renewExpiredSession(token)
                return request(method, path, body, refreshedToken, false)
            }
            if (status !in 200..299) {
                if (path == "/api/auth/google") {
                    // Show the server's exact HTTP status and raw response for Google login debugging.
                    throw IllegalStateException("HTTP $status — resposta do servidor: ${text.ifBlank { "(corpo vazio)" }}")
                }
                val serverMessage = json.optString("error").ifBlank { json.optString("message") }
                val diagnostic = if (path == "/api/posts" || path == "/api/files/upload-url" || path.endsWith("/complete")) {
                    "Falha ao publicar [${method} ${path}, HTTP ${status}]: " +
                        serverMessage.ifBlank { text.take(350).ifBlank { "Resposta vazia do servidor" } }
                } else serverMessage.ifBlank { "Falha na comunicação com o servidor (HTTP $status)." }
                throw IllegalStateException(diagnostic)
            }
            return json
        } catch (e: java.net.SocketTimeoutException) {
            throw IllegalStateException("O servidor demorou para responder. Tente novamente.")
        } catch (e: java.net.UnknownHostException) {
            throw IllegalStateException("Não foi possível encontrar o backend. Verifique a URL e a conexão.")
        } finally {
            connection.disconnect()
        }
    }
    private fun renewExpiredSession(expiredToken: String): String = synchronized(refreshLock) {
        val context = applicationContext
            ?: throw IllegalStateException("A sessão expirou. Abra novamente o aplicativo e entre na sua conta.")
        val prefs = context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE)
        val currentToken = prefs.getString("token", null).orEmpty()
        if (currentToken.isNotBlank() && currentToken != expiredToken) return@synchronized currentToken
        val refreshToken = prefs.getString("refresh_token", null).orEmpty()
        if (refreshToken.isBlank()) {
            throw IllegalStateException("Sua sessão expirou. Entre novamente com o Google para continuar.")
        }
        val result = try {
            request("POST", "/api/auth/refresh", JSONObject().put("refreshToken", refreshToken), null, false)
        } catch (_: Exception) {
            throw IllegalStateException("Sua sessão expirou. Entre novamente com o Google para continuar.")
        }
        val newToken = result.optString("token").ifBlank { result.optString("access_token") }
        val newRefreshToken = result.optString("refreshToken").ifBlank { result.optString("refresh_token") }
        if (newToken.isBlank() || newRefreshToken.isBlank()) {
            throw IllegalStateException("O servidor não conseguiu renovar sua sessão. Entre novamente com o Google.")
        }
        prefs.edit().putString("token", newToken).putString("refresh_token", newRefreshToken).apply()
        try {
            val accounts = org.json.JSONArray(prefs.getString("qinbly_accounts", "[]") ?: "[]")
            val username = prefs.getString("username", "").orEmpty()
            for (i in 0 until accounts.length()) {
                val account = accounts.optJSONObject(i) ?: continue
                if (account.optString("username").equals(username, true)) {
                    account.put("token", newToken)
                    account.put("refreshToken", newRefreshToken)
                }
            }
            prefs.edit().putString("qinbly_accounts", accounts.toString()).apply()
        } catch (_: Exception) { }
        newToken
    }

}
