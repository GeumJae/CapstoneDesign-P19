package com.example.capstone_mbti

import kotlinx.serialization.Serializable

// 1. 유저 (User)
@Serializable
data class User(
    val kakao_id: String,
    val nickname: String,
    val role: String = "user",
    val mbti: String? = null,
    val created_at: String? = null
)

@Serializable
data class UserMbtiUpdate(
    val mbti: String
)

// 2. MBTI 검사지 (MBTITest)
@Serializable
data class MBTITest(
    val id: Int? = null,
    val item: String,
    val answer_a: String,
    val answer_b: String,
    val mbti_type: String
)

// 3. 유저 선택 유형 (UserSelect)
@Serializable
data class UserSelect(
    val id: Long? = null,
    val kakao_id: String,
    val test_id: Int,
    val selected_answer: String
)

// 4. 카테고리 (Category)
@Serializable
data class Category(
    val id: Long? = null,
    val name: String
)

// 5. 게시글 (Board)
@Serializable
data class Board(
    val id: Long? = null,
    val title: String,
    val content: String,
    val category_id: Long? = null,
    val author_id: String,
    val created_at: String? = null,
    val User: User? = null,
    val Comment: List<CommentData>? = null,
    val BoardLike: List<BoardLike>? = null,
    val Option: List<Option>? = null
)

// 6. 투표 옵션 (Option)
@Serializable
data class Option(
    val id: Long? = null,
    val board_id: Long,
    val content: String,
    val VoteUser: List<VoteUser>? = null
)

// 7. 게시글 투표 유저 (VoteUser)
@Serializable
data class VoteUser(
    val id: Long? = null,
    val user_id: String,
    val option_id: Long
)

@Serializable
data class UserNicknameUpdate(
    val nickname: String
)

@Serializable
data class CommentData(
    val id: Long? = null,
    val board_id: Long,
    val author_id: String,
    val content: String,
    val created_at: String? = null,
    val User: User? = null
)

@Serializable
data class BoardLike(
    val id: Long? = null,
    val board_id: Long,
    val user_id: String
)