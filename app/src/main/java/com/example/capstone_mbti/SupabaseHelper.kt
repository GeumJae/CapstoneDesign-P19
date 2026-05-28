package com.example.capstone_mbti

import android.util.Log
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.function.Consumer
import io.github.jan.supabase.postgrest.query.Columns

object SupabaseHelper {
    private const val TAG = "SupabaseHelper"
    @JvmStatic
    fun updateUserMbti(kakaoId: String, mbti: String, onComplete: Runnable?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updateData = UserMbtiUpdate(mbti)
                SupabaseClient.client.postgrest["User"].update<UserMbtiUpdate>(updateData) {
                    eq("kakao_id", kakaoId)
                }
                Log.d(TAG, "MBTI 업데이트 성공: $mbti")
                launch(Dispatchers.Main) { onComplete?.run() }
            } catch (e: Exception) {
                Log.e(TAG, "MBTI 업데이트 실패: ${e.message}")
            }
        }
    }
    @JvmStatic
    fun fetchMbtiQuestions(onComplete: Consumer<List<MBTITest>>?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val questions = SupabaseClient.client.postgrest["MBTITest"]
                    .select().decodeList<MBTITest>()
                launch(Dispatchers.Main) { onComplete?.accept(questions) }
            } catch (e: Exception) {
                Log.e(TAG, "질문 로드 실패: ${e.message}")
                launch(Dispatchers.Main) { onComplete?.accept(emptyList()) }
            }
        }
    }
    @JvmStatic
    fun insertUserSelect(kakaoId: String, testId: Int, selectedAnswer: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userSelect = UserSelect(
                    kakao_id = kakaoId,
                    test_id = testId,
                    selected_answer = selectedAnswer
                )
                SupabaseClient.client.postgrest["UserSelect"].insert<UserSelect>(userSelect)
                Log.d(TAG, "선택지 저장 성공: $testId -> $selectedAnswer")
            } catch (e: Exception) {
                Log.e(TAG, "선택지 저장 실패: ${e.message}")
            }
        }
    }

    fun fetchCategories(onSuccess: (List<Category>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val categories = SupabaseClient.client.postgrest["Category"]
                    .select().decodeList<Category>()
                launch(Dispatchers.Main) { onSuccess(categories) }
            } catch (e: Exception) {
                Log.e(TAG, "카테고리 조회 실패: ${e.message}")
            }
        }
    }

    fun fetchBoards(categoryId: Long?, onSuccess: (List<Board>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val boards = SupabaseClient.client.postgrest["Board"].select(
                    columns = Columns.raw("*, User(*), Comment(*), BoardLike(*), Option(*)")
                ) {
                    if (categoryId != null) eq("category_id", categoryId)
                    order("created_at", order = Order.DESCENDING)
                }.decodeList<Board>()

                launch(Dispatchers.Main) { onSuccess(boards) }
            } catch (e: Exception) {
                Log.e(TAG, "게시글 조회 실패: ${e.message}")
            }
        }
    }

    fun createBoard(title: String, content: String, authorId: String, categoryId: Long? = null, onSuccess: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newBoard = Board(
                    title = title,
                    content = content,
                    author_id = authorId,
                    category_id = categoryId
                )
                SupabaseClient.client.postgrest["Board"].insert<Board>(newBoard)
                launch(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                Log.e(TAG, "게시글 작성 실패: ${e.message}")
            }
        }
    }

    fun fetchOptions(boardId: Long, onSuccess: (List<Option>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val options = SupabaseClient.client.postgrest["Option"].select {
                    eq("board_id", boardId)
                }.decodeList<Option>()
                launch(Dispatchers.Main) { onSuccess(options) }
            } catch (e: Exception) {
                Log.e(TAG, "투표 옵션 조회 실패: ${e.message}")
            }
        }
    }
    fun castVote(userId: String, clickedOptionId: Long, previousVote: VoteUser?, onSuccess: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (previousVote != null) {
                    SupabaseClient.client.postgrest["VoteUser"].delete {
                        eq("id", previousVote.id!!)
                    }

                    if (previousVote.option_id != clickedOptionId) {
                        SupabaseClient.client.postgrest["VoteUser"].insert(
                            VoteUser(user_id = userId, option_id = clickedOptionId)
                        )
                    }
                } else {
                    SupabaseClient.client.postgrest["VoteUser"].insert(
                        VoteUser(user_id = userId, option_id = clickedOptionId)
                    )
                }

                launch(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                Log.e(TAG, "투표 처리 실패: ${e.message}")
            }
        }
    }
    fun updateNickname(kakaoId: String, newNickname: String, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updateData = UserNicknameUpdate(newNickname)
                SupabaseClient.client.postgrest["User"].update<UserNicknameUpdate>(updateData) {
                    eq("kakao_id", kakaoId)
                }
                launch(Dispatchers.Main) { onComplete(true) }
            } catch (e: Exception) {
                Log.e(TAG, "닉네임 업데이트 실패: ${e.message}")
                launch(Dispatchers.Main) { onComplete(false) }
            }
        }
    }
    fun deleteUser(kakaoId: String, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SupabaseClient.client.postgrest["User"].delete {
                    eq("kakao_id", kakaoId)
                }
                launch(Dispatchers.Main) { onComplete(true) }
            } catch (e: Exception) {
                Log.e(TAG, "회원 데이터 삭제 실패: ${e.message}")
                launch(Dispatchers.Main) { onComplete(false) }
            }
        }
    }

    fun fetchComments(boardId: Long, onSuccess: (List<CommentData>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val comments = SupabaseClient.client.postgrest["Comment"].select(columns = Columns.raw("*, User(*)")) {
                    eq("board_id", boardId)
                    order("created_at", order = Order.ASCENDING) // 옛날 댓글이 위로 오도록 오름차순 정렬
                }.decodeList<CommentData>()

                launch(Dispatchers.Main) { onSuccess(comments) }
            } catch (e: Exception) {
                Log.e(TAG, "댓글 조회 실패: ${e.message}")
            }
        }
    }
    fun createComment(boardId: Long, authorId: String, content: String, onSuccess: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newComment = CommentData(
                    board_id = boardId,
                    author_id = authorId,
                    content = content
                )
                SupabaseClient.client.postgrest["Comment"].insert<CommentData>(newComment)
                launch(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                Log.e(TAG, "댓글 작성 실패: ${e.message}")
            }
        }
    }

    fun toggleLike(boardId: Long, userId: String, isCurrentlyLiked: Boolean, onComplete: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isCurrentlyLiked) {
                    SupabaseClient.client.postgrest["BoardLike"].delete {
                        eq("board_id", boardId)
                        eq("user_id", userId)
                    }
                } else {
                    SupabaseClient.client.postgrest["BoardLike"].insert(
                        BoardLike(board_id = boardId, user_id = userId)
                    )
                }
                launch(Dispatchers.Main) { onComplete() }
            } catch (e: Exception) {
                Log.e("Supabase", "좋아요 처리 실패: ${e.message}")
            }
        }
    }
    fun deleteBoard(boardId: Long, onSuccess: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SupabaseClient.client.postgrest["Board"].delete { eq("id", boardId) }
                launch(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) { Log.e(TAG, "게시글 삭제 실패: ${e.message}") }
        }
    }
    fun deleteComment(commentId: Long, onSuccess: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SupabaseClient.client.postgrest["Comment"].delete { eq("id", commentId) }
                launch(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) { Log.e(TAG, "댓글 삭제 실패: ${e.message}") }
        }
    }
    fun fetchOptionsWithVotes(boardId: Long, onSuccess: (List<Option>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val options = SupabaseClient.client.postgrest["Option"]
                    .select(columns = Columns.raw("*, VoteUser(*)")) {
                        eq("board_id", boardId)
                        order("id", order = Order.ASCENDING)
                    }.decodeList<Option>()
                launch(Dispatchers.Main) { onSuccess(options) }
            } catch (e: Exception) {
                Log.e(TAG, "투표 현황 조회 실패: ${e.message}")
            }
        }
    }
    fun createBoardWithOptions(
        title: String,
        content: String,
        authorId: String,
        categoryId: Long?,
        options: List<String>,
        onSuccess: () -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newBoard = Board(
                    title = title,
                    content = content,
                    author_id = authorId,
                    category_id = categoryId
                )
                SupabaseClient.client.postgrest["Board"].insert<Board>(newBoard)

                val insertedBoardList = SupabaseClient.client.postgrest["Board"].select {
                    eq("author_id", authorId)
                    order("created_at", order = Order.DESCENDING)
                }.decodeList<Board>()

                val boardId = insertedBoardList.firstOrNull()?.id

                if (boardId != null && options.isNotEmpty()) {
                    val optionObjects = options.filter { it.isNotBlank() }.map {
                        Option(board_id = boardId, content = it)
                    }

                    if (optionObjects.isNotEmpty()) {
                        SupabaseClient.client.postgrest["Option"].insert(optionObjects)
                    }
                }
                launch(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                Log.e("SupabaseHelper", "게시글 및 투표 작성 실패: ${e.message}")
            }
        }
    }
}