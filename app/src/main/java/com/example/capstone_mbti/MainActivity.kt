package com.example.capstone_mbti

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.util.Log
import androidx.compose.material3.AlertDialog
import com.kakao.sdk.user.UserApiClient
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import io.github.jan.supabase.postgrest.postgrest
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

data class Post(
    val id: Int,
    val mbti: String,
    val author: String,
    val authorId: String,
    val category: String,
    val title: String,
    val preview: String,
    val content: String,
    val likes: Int,
    val comments: Int,
    val badgeColor: Color,
    val badgeTextColor: Color,
    val isLikedByMe: Boolean,
    val hasPoll: Boolean
)

data class CategoryItem(
    val emoji: String,
    val title: String,
    val count: String,
    val color: Color
)

data class MbtiGroup(
    val mbti: String,
    val name: String,
    val color: Color
)

sealed class Screen {
    object Home : Screen()
    object Category : Screen()
    object Profile : Screen()
    data class Detail(val post: Post) : Screen()
    object Write : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MBTIApp()
        }
    }
}

@Composable
fun MBTIApp() {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val myKakaoId = remember { authManager.getLoginSession()?.replace("kakao_", "") ?: "" }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedFilter by remember { mutableStateOf("전체") }
    var myUser by remember { mutableStateOf<User?>(null) }
    var myPostCount by remember { mutableStateOf(0) }
    var myCommentCount by remember { mutableStateOf(0) }
    var myLikeCount by remember { mutableStateOf(0) }

    LaunchedEffect(myKakaoId, currentScreen) {
        if (myKakaoId.isNotEmpty()) {
            try {
                val users = SupabaseClient.client.postgrest["User"].select {
                    eq("kakao_id", myKakaoId)
                }.decodeList<User>()
                if (users.isNotEmpty()) {
                    myUser = users[0]
                }

                if (currentScreen == Screen.Profile) {
                    val myBoards = SupabaseClient.client.postgrest["Board"].select {
                        eq("author_id", myKakaoId)
                    }.decodeList<Board>()
                    myPostCount = myBoards.size

                    val myComments = SupabaseClient.client.postgrest["Comment"].select {
                        eq("author_id", myKakaoId)
                    }.decodeList<CommentData>()
                    myCommentCount = myComments.size

                    val myLikes = SupabaseClient.client.postgrest["BoardLike"].select {
                        eq("user_id", myKakaoId)
                    }.decodeList<BoardLike>()
                    myLikeCount = myLikes.size
                }

            } catch (e: Exception) {
                Log.e("MBTIApp", "프로필 통계 갱신 실패: ${e.message}")
            }
        }
    }
    val myNickname = myUser?.nickname ?: "로딩중..."
    val myMbti = myUser?.mbti ?: "미정"
    val myTypeName = when(myMbti) {
        "ISTJ" -> "현실주의자"
        "ISFJ" -> "수호자"
        "INFJ" -> "옹호자"
        "INTJ" -> "전략가"
        "ISTP" -> "장인"
        "ISFP" -> "모험가"
        "INFP" -> "중재자"
        "INTP" -> "논리술사"
        "ESTP" -> "활동가"
        "ESFP" -> "연예인"
        "ENFP" -> "활동가"
        "ENTP" -> "변론가"
        "ESTJ" -> "경영자"
        "ESFJ" -> "집정관"
        "ENFJ" -> "선도자"
        "ENTJ" -> "통솔자"
        else -> "커뮤니티 멤버"
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F8FA)
    ) {
        when (val screen = currentScreen) {
            is Screen.Home -> HomeScreen(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                onPostClick = { currentScreen = Screen.Detail(it) },
                onWriteClick = { currentScreen = Screen.Write },
                onHomeClick = {
                    selectedFilter = "전체"
                    currentScreen = Screen.Home
                },
                onCategoryClick = { currentScreen = Screen.Category },
                onProfileClick = { currentScreen = Screen.Profile }
            )

            is Screen.Category -> CategoryScreen(
                onCategorySelected = { category ->
                    selectedFilter = category
                    currentScreen = Screen.Home
                },
                onHomeClick = {
                    selectedFilter = "전체"
                    currentScreen = Screen.Home
                },
                onCategoryClick = { currentScreen = Screen.Category },
                onProfileClick = { currentScreen = Screen.Profile }
            )

            is Screen.Profile -> ProfileScreen(
                nickname = myNickname,
                mbti = myMbti,
                typeName = myTypeName,
                postCount = myPostCount.toString(),
                commentCount = myCommentCount.toString(),
                likeCount = myLikeCount.toString(),
                onSaveNickname = { newName ->
                    SupabaseHelper.updateNickname(myKakaoId, newName) { success ->
                        if (success) {
                            myUser = myUser?.copy(nickname = newName)
                            Toast.makeText(context, "닉네임이 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "닉네임 변경에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onSaveMbti = { newMbti ->
                    SupabaseHelper.updateUserMbti(myKakaoId, newMbti, Runnable {
                        val newTypeName = when(newMbti) {
                            "ISTJ" -> "전략가"
                            "ISFJ" -> "수호자"
                            "INFJ" -> "옹호자"
                            "INTJ" -> "전략가"
                            "ISTP" -> "장인"
                            "ISFP" -> "예술가"
                            "INFP" -> "중재자"
                            "INTP" -> "분석가"
                            "ESTP" -> "활동가"
                            "ESFP" -> "연예인"
                            "ENFP" -> "활동가"
                            "ENTP" -> "변론가"
                            "ESTJ" -> "경영자"
                            "ESFJ" -> "외교관"
                            "ENFJ" -> "선도자"
                            "ENTJ" -> "지도자"
                            else -> ""
                        }
                        myUser = myUser?.copy(mbti = newMbti)
                        Toast.makeText(context, "MBTI가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    })
                },
                onDeleteAccount = {
                    SupabaseHelper.deleteUser(myKakaoId) { success ->
                        if (success) {
                            UserApiClient.instance.unlink { _ ->
                                authManager.logout()
                                Toast.makeText(context, "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                                context.startActivity(Intent(context, LoginActivity::class.java))
                            }
                        } else {
                            Toast.makeText(context, "탈퇴 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onHomeClick = {
                    selectedFilter = "전체"
                    currentScreen = Screen.Home
                },
                onCategoryClick = { currentScreen = Screen.Category },
                onProfileClick = { currentScreen = Screen.Profile },
                onActivityMenuClick = { filterName ->
                    selectedFilter = filterName
                    currentScreen = Screen.Home
                }
            )

            is Screen.Detail -> {
                var dbComments by remember { mutableStateOf<List<CommentData>>(emptyList()) }
                var pollOptions by remember { mutableStateOf<List<Option>>(emptyList()) }
                var isLiked by remember { mutableStateOf(screen.post.isLikedByMe) }
                var likeCount by remember { mutableStateOf(screen.post.likes) }

                LaunchedEffect(screen.post.id) {
                    SupabaseHelper.fetchComments(screen.post.id.toLong()) { fetched ->
                        dbComments = fetched
                    }
                    if (screen.post.hasPoll) {
                        SupabaseHelper.fetchOptionsWithVotes(screen.post.id.toLong()) { fetched ->
                            pollOptions = fetched
                        }
                    }
                }

                DetailScreen(
                    post = screen.post,
                    comments = dbComments,
                    pollOptions = pollOptions,
                    myKakaoId = myKakaoId,
                    isLiked = isLiked,
                    likeCount = likeCount,
                    onBack = { currentScreen = Screen.Home },
                    onDeleteClick = {
                        SupabaseHelper.deleteBoard(screen.post.id.toLong()) {
                            Toast.makeText(context, "게시글이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                            selectedFilter = selectedFilter
                            currentScreen = Screen.Home
                        }
                    },
                    onLikeClick = {
                        val currentlyLiked = isLiked
                        isLiked = !currentlyLiked
                        likeCount = if (!currentlyLiked) likeCount + 1 else likeCount - 1
                        SupabaseHelper.toggleLike(screen.post.id.toLong(), myKakaoId, currentlyLiked) {}
                    },
                    onVoteClick = { clickedOptionId ->
                        val myPreviousVote = pollOptions
                            .flatMap { it.VoteUser ?: emptyList() }
                            .find { it.user_id == myKakaoId }

                        SupabaseHelper.castVote(myKakaoId, clickedOptionId, myPreviousVote) {
                            Toast.makeText(context, "투표가 반영되었습니다.", Toast.LENGTH_SHORT).show()
                            SupabaseHelper.fetchOptionsWithVotes(screen.post.id.toLong()) { fetched ->
                                pollOptions = fetched
                            }
                        }
                    },
                    onAddComment = { content ->
                        SupabaseHelper.createComment(boardId = screen.post.id.toLong(), authorId = myKakaoId, content = content) {
                            SupabaseHelper.fetchComments(screen.post.id.toLong()) { fetched -> dbComments = fetched }
                        }
                    },
                    onDeleteComment = { index ->
                        val commentId = dbComments[index].id ?: return@DetailScreen
                        SupabaseHelper.deleteComment(commentId) {
                            Toast.makeText(context, "댓글이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                            SupabaseHelper.fetchComments(screen.post.id.toLong()) { fetched -> dbComments = fetched }
                        }
                    }
                )
            }

            is Screen.Write -> WriteScreen(
                onBack = { currentScreen = Screen.Home },
                onSave = { categoryName, title, content, voteOptions ->
                    SupabaseHelper.fetchCategories { cats ->
                        val targetCatId = cats.find { it.name == categoryName }?.id
                        SupabaseHelper.createBoardWithOptions(
                            title = title,
                            content = content,
                            authorId = myKakaoId,
                            categoryId = targetCatId,
                            options = voteOptions
                        ) {
                            selectedFilter = "전체"
                            currentScreen = Screen.Home
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    onPostClick: (Post) -> Unit,
    onWriteClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val myKakaoId = remember { AuthManager(context).getLoginSession()?.replace("kakao_", "") ?: "" }

    var dbPosts by remember { mutableStateOf<List<Board>>(emptyList()) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }

    LaunchedEffect(selectedFilter) {
        SupabaseHelper.fetchCategories { fetchedCats ->
            categories = fetchedCats
            val targetCat = fetchedCats.find { it.name == selectedFilter }

            SupabaseHelper.fetchBoards(targetCat?.id) { fetchedBoards ->
                dbPosts = when (selectedFilter) {
                    "전체", "인기" -> fetchedBoards.filter { (it.BoardLike?.size ?: 0) >= 10 }
                    "내가 쓴 글" -> fetchedBoards.filter { it.author_id == myKakaoId }
                    "내가 쓴 댓글" -> fetchedBoards.filter { board ->
                        board.Comment?.any { comment -> comment.author_id == myKakaoId } == true
                    }
                    "좋아요 한 글" -> fetchedBoards.filter { board ->
                        board.BoardLike?.any { like -> like.user_id == myKakaoId } == true
                    }
                    else -> {
                        if (targetCat != null) fetchedBoards
                        else fetchedBoards.filter { it.User?.mbti == selectedFilter }
                    }
                }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 24.dp, end = 24.dp, top = 34.dp, bottom = 22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎭", fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "MBTI",
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF111827)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip("전체", selectedFilter == "전체") { onFilterSelected("전체") }
                    FilterChip("⌁ 인기", selectedFilter == "인기") { onFilterSelected("인기") }
                }

                if (selectedFilter != "전체" && selectedFilter != "인기") {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "$selectedFilter 게시글",
                        fontSize = 18.sp,
                        color = Color(0xFF9B12F0),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(dbPosts) { board ->
                    val myKakaoId = AuthManager(LocalContext.current).getLoginSession()?.replace("kakao_", "") ?: ""
                    val isLiked = board.BoardLike?.any { it.user_id == myKakaoId } == true
                    val actualCategory = categories.find { it.id == board.category_id }?.name ?: "기타"

                    val convertedPost = Post(
                        id = board.id?.toInt() ?: 0,
                        mbti = board.User?.mbti ?: "미정",
                        author = board.User?.nickname ?: "알 수 없음",
                        authorId = board.author_id,
                        category = actualCategory,
                        title = board.title,
                        preview = board.content,
                        content = board.content,
                        likes = board.BoardLike?.size ?: 0,
                        comments = board.Comment?.size ?: 0,
                        isLikedByMe = isLiked,
                        hasPoll = board.Option?.isNotEmpty() == true,
                        badgeColor = Color(0xFFE8FAD3),
                        badgeTextColor = Color(0xFF58A51D)
                    )

                    HomePostItem(
                        post = convertedPost,
                        myKakaoId = myKakaoId,
                        onClick = { onPostClick(convertedPost) }
                    )
                }
            }

            BottomNavBar(
                selected = "홈",
                onHomeClick = onHomeClick,
                onCategoryClick = onCategoryClick,
                onProfileClick = onProfileClick
            )
        }

        FloatingActionButton(
            onClick = onWriteClick,
            containerColor = Color(0xFF9B12F0),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 26.dp, end = 28.dp)
                .size(58.dp)
        ) {
            Text("+", fontSize = 32.sp, fontWeight = FontWeight.Light)
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFF9B12F0) else Color(0xFFF0F1F4)
    val fg = if (selected) Color.White else Color(0xFF4B5563)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            color = fg,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HomePostItem(
    post: Post,
    myKakaoId: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(post.badgeColor)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = post.mbti,
                    fontSize = 14.sp,
                    color = post.badgeTextColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = post.author,
                fontSize = 18.sp,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.weight(1f))

            if (post.hasPoll) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFDCEBFF))
                        .padding(horizontal = 13.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "투표",
                        fontSize = 15.sp,
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = post.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = post.preview,
            fontSize = 18.sp,
            color = Color(0xFF6B7280),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (post.isLikedByMe) "♥ ${post.likes}" else "♡ ${post.likes}",
                fontSize = 17.sp,
                color = if (post.isLikedByMe) Color(0xFFFF3B3B) else Color(0xFF6B7280)
            )
            Spacer(modifier = Modifier.width(22.dp))
            Text("💬 ${post.comments}", fontSize = 17.sp, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.weight(1f))
            Text(post.category, fontSize = 16.sp, color = Color(0xFF6B7280))
        }
    }

    Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
}

@Composable
fun CategoryScreen(
    onCategorySelected: (String) -> Unit,
    onHomeClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val categories = listOf(
        CategoryItem("☕", "일상", "1234개 게시글", Color(0xFFFFF7BC)),
        CategoryItem("☁️", "고민상담", "856개 게시글", Color(0xFFDDEBFF)),
        CategoryItem("❓", "질문", "642개 게시글", Color(0xFFF1E7FF)),
        CategoryItem("💡", "정보", "428개 게시글", Color(0xFFDDF8E4)),
        CategoryItem("😄", "유머", "912개 게시글", Color(0xFFFDEAF5)),
        CategoryItem("💝", "연애", "567개 게시글", Color(0xFFFFE4E4)),
        CategoryItem("💼", "직장", "389개 게시글", Color(0xFFE3E9FF)),
        CategoryItem("🎨", "취미", "234개 게시글", Color(0xFFD7FAFB))
    )

    val groups = listOf(
        MbtiGroup("INTJ", "전략가", Color(0xFF9B12F0)),
        MbtiGroup("ENFP", "활동가",  Color(0xFF4CAB00)),
        MbtiGroup("INFP", "중재자",  Color(0xFF0096B7)),
        MbtiGroup("ESTJ", "경영자",  Color(0xFFD90014)),
        MbtiGroup("ISTJ", "현실주의자",  Color(0xFF2C3E50)),
        MbtiGroup("ISFJ", "수호자", Color(0xFF34495E)),
        MbtiGroup("INFJ", "옹호자",  Color(0xFF8E44AD)),
        MbtiGroup("INTP", "논리술사", Color(0xFF7F8C8D)),
        MbtiGroup("ISTP", "장인", Color(0xFF95A5A6)),
        MbtiGroup("ISFP", "모험가", Color(0xFF27AE60)),
        MbtiGroup("INFP", "중재자", Color(0xFF2ECC71)),
        MbtiGroup("ENTJ", "통솔자",  Color(0xFFC0392B)),
        MbtiGroup("ENTP", "변론가",  Color(0xFFE67E22)),
        MbtiGroup("ENFJ", "선도자", Color(0xFFF1C40F)),
        MbtiGroup("ESFP", "연예인", Color(0xFFE74C3C)),
        MbtiGroup("ESFJ", "집정관", Color(0xFFD35400))
    )

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(start = 24.dp, end = 24.dp, top = 34.dp, bottom = 28.dp)
                ) {
                    Text(
                        text = "카테고리",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF111827)
                    )
                }

                Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)

                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                    Text(
                        text = "주제별",
                        fontSize = 20.sp,
                        color = Color(0xFF374151)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    for (i in categories.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CategoryCard(
                                item = categories[i],
                                modifier = Modifier.weight(1f),
                                onClick = { onCategorySelected(categories[i].title) }
                            )

                            CategoryCard(
                                item = categories[i + 1],
                                modifier = Modifier.weight(1f),
                                onClick = { onCategorySelected(categories[i + 1].title) }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "MBTI별 모임",
                        fontSize = 20.sp,
                        color = Color(0xFF374151)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    groups.forEach { group ->
                        MbtiGroupCard(
                            group = group,
                            onClick = { onCategorySelected(group.mbti) }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }

        BottomNavBar(
            selected = "카테고리",
            onHomeClick = onHomeClick,
            onCategoryClick = onCategoryClick,
            onProfileClick = onProfileClick
        )
    }
}

@Composable
fun CategoryCard(
    item: CategoryItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = item.color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, top = 22.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(item.emoji, fontSize = 33.sp)

            Column {
                Text(
                    text = item.title,
                    fontSize = 24.sp,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.count,
                    fontSize = 17.sp,
                    color = Color(0xFF374151)
                )
            }
        }
    }
}

@Composable
fun MbtiGroupCard(
    group: MbtiGroup,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(group.color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.mbti,
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column {
                Text(
                    text = "${group.mbti} - ${group.name}",
                    fontSize = 22.sp,
                    color = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text("›", fontSize = 42.sp, color = Color(0xFF9CA3AF))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    nickname: String,
    mbti: String,
    typeName: String,
    postCount: String,
    commentCount: String,
    likeCount: String,
    onSaveNickname: (String) -> Unit,
    onSaveMbti: (String) -> Unit,
    onHomeClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDeleteAccount: () -> Unit,
    onActivityMenuClick: (String) -> Unit
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    var editNickname by remember { mutableStateOf(nickname) }
    var showFirstDialog by remember { mutableStateOf(false) }
    var showSecondDialog by remember { mutableStateOf(false) }
    var showNotification by remember { mutableStateOf(false) }
    var showPolicy by remember { mutableStateOf(false) }
    var showMbtiDialog by remember { mutableStateOf(false) }
    if (showPolicy) {
        PolicyDialog(onDismiss = { showPolicy = false })
    }
    if (showNotification) {
        NotificationDialog(onDismiss = { showNotification = false })
    }
    if (showMbtiDialog) {
        MbtiChangeDialog(
            currentMbti = mbti,
            onDismiss = { showMbtiDialog = false },
            onSelect = { newMbti ->
                onSaveMbti(newMbti)
                showMbtiDialog = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(start = 28.dp, end = 28.dp, top = 34.dp, bottom = 28.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "프로필",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF111827)
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val mbtiImageRes = when(mbti.uppercase()) {
                            "INTJ" -> R.drawable.emoji_intj
                            "INTP" -> R.drawable.emoji_intp
                            "ENTJ" -> R.drawable.emoji_entj
                            "ENTP" -> R.drawable.emoji_entp
                            "INFJ" -> R.drawable.emoji_infj
                            "INFP" -> R.drawable.emoji_infp
                            "ENFJ" -> R.drawable.emoji_enfj
                            "ENFP" -> R.drawable.emoji_enfp
                            "ISTJ" -> R.drawable.emoji_istj
                            "ISFJ" -> R.drawable.emoji_isfj
                            "ESTJ" -> R.drawable.emoji_estj
                            "ESFJ" -> R.drawable.emoji_esfj
                            "ISTP" -> R.drawable.emoji_istp
                            "ISFP" -> R.drawable.emoji_isfp
                            "ESTP" -> R.drawable.emoji_estp
                            "ESFP" -> R.drawable.emoji_esfp
                            else -> R.drawable.emoji_estj
                        }
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = mbtiImageRes),
                            contentDescription = "MBTI 캐릭터 이미지",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFF3F4F6)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(28.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = mbti,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE5E7EB), shape = RoundedCornerShape(6.dp))
                                        .clickable { showMbtiDialog = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "변경", fontSize = 12.sp, color = Color(0xFF4B5563))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = typeName,
                                fontSize = 15.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(38.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        ProfileStat(postCount, "게시글", Modifier.weight(1f))
                        ProfileStat(commentCount, "댓글", Modifier.weight(1f))
                        ProfileStat(likeCount, "좋아요", Modifier.weight(1f))
                    }
                }

                Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 24.dp)
                ) {
                    Text("활동", fontSize = 18.sp, color = Color(0xFF374151))
                    Spacer(modifier = Modifier.height(14.dp))

                    ProfileMenuItem("내가 쓴 글", onClick = { onActivityMenuClick("내가 쓴 글") })
                    ProfileMenuItem("내가 쓴 댓글", onClick = { onActivityMenuClick("내가 쓴 댓글") })
                    ProfileMenuItem("좋아요 한 글", onClick = { onActivityMenuClick("좋아요 한 글") })
                    Spacer(modifier = Modifier.height(28.dp))

                    Text("설정", fontSize = 18.sp, color = Color(0xFF374151))
                    Spacer(modifier = Modifier.height(14.dp))
                    ProfileMenuItem("알림 설정", onClick = {
                        showNotification = true
                    })
                    ProfileMenuItem("개인정보 처리방침", onClick = {
                        showPolicy = true
                    })

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val authManager = AuthManager(context)
                                authManager.logout()

                                val intent = Intent(context, LoginActivity::class.java)
                                context.startActivity(intent)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .padding(horizontal = 22.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "로그아웃",
                                fontSize = 23.sp,
                                color = Color(0xFFFF3B3B)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = "↪",
                                fontSize = 30.sp,
                                color = Color(0xFFFF3B3B)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showFirstDialog = true
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .padding(horizontal = 22.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("회원탈퇴", fontSize = 23.sp, color = Color(0xFFFF3B3B))
                            Spacer(modifier = Modifier.weight(1f))
                            Text("›", fontSize = 34.sp, color = Color(0xFFFF3B3B))
                        }
                    }

                    if (showFirstDialog) {
                        AlertDialog(
                            onDismissRequest = { showFirstDialog = false },
                            title = { Text("회원탈퇴") },
                            text = { Text("정말 탈퇴하시겠습니까?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showFirstDialog = false
                                    showSecondDialog = true
                                }) {
                                    Text("네")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showFirstDialog = false
                                }) {
                                    Text("아니요")
                                }
                            }
                        )
                    }

                    if (showSecondDialog) {
                        AlertDialog(
                            onDismissRequest = { showSecondDialog = false },
                            title = { Text("탈퇴 전 확인") },
                            text = {
                                Text(
                                    "회원탈퇴 시 로그인 정보가 삭제되며,\n" +
                                            "카카오 계정과 앱의 연결이 해제됩니다.\n\n" +
                                            "정말 탈퇴하시려면 아래 버튼을 눌러주세요."
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    onDeleteAccount()
                                }) {
                                    Text("탈퇴하겠습니다")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showSecondDialog = false
                                }) {
                                    Text("취소")
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = editNickname,
                            onValueChange = { editNickname = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("닉네임") },
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val newNickname = if (editNickname.isBlank()) nickname else editNickname
                                onSaveNickname(newNickname)
                                isEditing = false
                                Toast.makeText(context, "닉네임이 수정되었습니다", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9B12F0),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("닉네임 저장", fontSize = 16.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                editNickname = nickname
                                isEditing = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9B12F0),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("닉네임 수정", fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        BottomNavBar(
            selected = "프로필",
            onHomeClick = onHomeClick,
            onCategoryClick = onCategoryClick,
            onProfileClick = onProfileClick
        )
    }
}

@Composable
fun ProfileStat(
    number: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(number, fontSize = 29.sp, color = Color(0xFF111827))
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 18.sp, color = Color(0xFF4B5563))
    }
}

@Composable
fun ProfileMenuItem(title: String,
                    onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable {onClick()},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 23.sp,
                color = Color(0xFF111827)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text("›", fontSize = 38.sp, color = Color(0xFF9CA3AF))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    post: Post,
    comments: List<CommentData>,
    pollOptions: List<Option>,
    myKakaoId: String,
    isLiked: Boolean,
    likeCount: Int,
    onBack: () -> Unit,
    onDeleteClick: () -> Unit,
    onLikeClick: () -> Unit,
    onVoteClick: (Long) -> Unit,
    onAddComment: (String) -> Unit,
    onDeleteComment: (Int) -> Unit
) {
    val context = LocalContext.current
    var commentInput by remember { mutableStateOf("") }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.width(36.dp)
            ) {
                Text("←", fontSize = 24.sp, color = Color.Black)
            }
            Text(
                text = "게시글",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Spacer(modifier = Modifier.weight(1f))

            if (post.authorId == myKakaoId) {
                TextButton(
                    onClick = onDeleteClick,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text("삭제", color = Color(0xFFFF3B3B), fontSize = 15.sp)
                }
            }
        }

        Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3E8FF))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = post.mbti,
                                fontSize = 12.sp,
                                color = Color(0xFF9B12F0),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(text = post.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = post.content, fontSize = 17.sp, lineHeight = 26.sp, color = Color(0xFF374151))

                    Spacer(modifier = Modifier.height(24.dp))

                    if (post.hasPoll && pollOptions.isNotEmpty()) {
                        val totalVotes = pollOptions.sumOf { it.VoteUser?.size ?: 0 }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F5FF)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("투표", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Spacer(modifier = Modifier.height(16.dp))

                                pollOptions.forEach { option ->
                                    val voteCount = option.VoteUser?.size ?: 0
                                    val percentage = if (totalVotes > 0) (voteCount.toFloat() / totalVotes * 100).toInt() else 0
                                    val isMyVote = option.VoteUser?.any { it.user_id == myKakaoId } == true

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White)
                                            .clickable { onVoteClick(option.id!!) }
                                    ) {
                                        if (percentage > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(fraction = percentage / 100f)
                                                    .background(if (isMyVote) Color(0xFFC4B5FD) else Color(0xFFDBEAFE))
                                            )
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = option.content,
                                                fontSize = 15.sp,
                                                color = if (isMyVote) Color(0xFF4C1D95) else Color(0xFF374151),
                                                fontWeight = if (isMyVote) FontWeight.Medium else FontWeight.Normal
                                            )
                                            Text(text = "${percentage}%", fontSize = 14.sp, color = Color(0xFF6B7280))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "총 ${totalVotes}명 참여",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontSize = 13.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            modifier = Modifier.clickable { onLikeClick() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isLiked) "👍" else "👍🏻",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "$likeCount", fontSize = 16.sp, color = Color(0xFF4B5563))
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💬", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${comments.size}", fontSize = 16.sp, color = Color(0xFF4B5563))
                        }
                    }
                }
            }

            item { Divider(color = Color(0xFFF9FAFB), thickness = 8.dp) }

            item {
                Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
                    Text(text = "댓글 ${comments.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            if (comments.isEmpty()) {
                item {
                    Text(
                        text = "아직 댓글이 없습니다. 첫 댓글을 남겨보세요!",
                        modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 15.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            } else {
                itemsIndexed(comments) { index, comment ->
                    val commenterNickname = comment.User?.nickname ?: "알 수 없음"
                    val commenterMbti = comment.User?.mbti ?: ""

                    Column(modifier = Modifier.padding(horizontal = 22.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF3E8FF))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = commenterMbti, fontSize = 11.sp, color = Color(0xFF9B12F0), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = commenterNickname, fontSize = 14.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = comment.content, color = Color(0xFF374151), fontSize = 15.sp, lineHeight = 22.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (comment.author_id == myKakaoId) {
                                TextButton(
                                    onClick = { onDeleteComment(index) },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("삭제", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    Divider(color = Color(0xFFF3F4F6), thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentInput,
                    onValueChange = { commentInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("댓글을 입력하세요...", color = Color(0xFF9CA3AF), fontSize = 15.sp) },
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = Color(0xFF9B12F0),
                        containerColor = Color(0xFFF9FAFB)
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        if (commentInput.isNotBlank()) {
                            onAddComment(commentInput)
                            commentInput = ""
                            Toast.makeText(context, "댓글이 등록되었습니다", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B12F0)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text("등록", fontSize = 15.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    onBack: () -> Unit,
    onSave: (String, String, String, List<String>) -> Unit
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    val categories = listOf("일상", "고민상담", "질문", "정보", "유머", "연애", "직장", "취미")
    var category by remember { mutableStateOf(categories[0]) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    var isVoteEnabled by remember { mutableStateOf(false) }
    var voteOptions by remember { mutableStateOf(listOf("", "")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                Text("←", fontSize = 28.sp, color = Color.Black)
            }
            Text("글쓰기", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            TextButton(onClick = {
                if (title.isBlank() || content.isBlank()) {
                    Toast.makeText(context, "제목과 내용을 입력하세요", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }
                if (isVoteEnabled && voteOptions.any { it.isBlank() }) {
                    Toast.makeText(context, "투표 선택지를 모두 입력하세요", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }
                Toast.makeText(context, "글이 등록되었습니다", Toast.LENGTH_SHORT).show()
                val finalOptions = if (isVoteEnabled) voteOptions else emptyList()
                onSave(category, title, content, finalOptions)
            }) {
                Text("완료", fontSize = 18.sp, color = Color(0xFF9B12F0))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("카테고리", fontSize = 16.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                FilterChip(cat, category == cat) { category = cat }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("제목을 입력하세요", color = Color(0xFF9CA3AF)) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                containerColor = Color.Transparent
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 22.sp)
        )

        Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("내용을 입력하세요", color = Color(0xFF9CA3AF)) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                containerColor = Color.Transparent
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
        )

        Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("투표 추가", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
            Switch(
                checked = isVoteEnabled,
                onCheckedChange = { isVoteEnabled = it },
                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF9B12F0))
            )
        }

        if (isVoteEnabled) {
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxHeight(0.4f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(voteOptions) { index, option ->
                    OutlinedTextField(
                        value = option,
                        onValueChange = { newValue ->
                            val updatedOptions = voteOptions.toMutableList()
                            updatedOptions[index] = newValue
                            voteOptions = updatedOptions
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("선택지 ${index + 1}", color = Color(0xFF9CA3AF)) },
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color(0xFFF3F4F6),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color(0xFF9B12F0)
                        )
                    )
                }

                item {
                    OutlinedButton(
                        onClick = { voteOptions = voteOptions + "" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B7280))
                    ) {
                        Text("+ 선택지 추가", fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    selected: String,
    onHomeClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .background(Color.White)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = "⌂",
            label = "홈",
            selected = selected == "홈",
            modifier = Modifier.weight(1f),
            onClick = onHomeClick
        )

        BottomNavItem(
            icon = "▦",
            label = "카테고리",
            selected = selected == "카테고리",
            modifier = Modifier.weight(1f),
            onClick = onCategoryClick
        )

        BottomNavItem(
            icon = "♙",
            label = "프로필",
            selected = selected == "프로필",
            modifier = Modifier.weight(1f),
            onClick = onProfileClick
        )
    }
}

@Composable
fun BottomNavItem(
    icon: String,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val color = if (selected) Color(0xFF9B12F0) else Color(0xFF9CA3AF)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            fontSize = 28.sp,
            color = color,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontSize = 14.sp,
            color = color,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun PolicyDialog(onDismiss: () -> Unit) {
    val scrollState = rememberScrollState()

    val privacyPolicyText = """
        <프로젝트명 또는 앱 이름>은 정보주체의 개인정보를 안전하게 처리하기 위해 최선을 다하고 있습니다. 본 방침은 대학 프로젝트 개발 및 테스트 목적에 맞춰 필요 최소한의 데이터 처리 기준을 안내합니다.
        
        1. 처리하는 개인정보 항목 및 목적
        앱은 서비스 기능 구현을 위해 Supabase DB에 연동된 다음의 최소 정보만을 수집하고 이용합니다.
        
        - 카카오 로그인 및 회원 관리 (동의 없이 처리): 카카오 고유회원번호(kakao_id), 닉네임(nickname), 가입일자
          (근거: 「개인정보 보호법」 제15조 제1항 제4호 계약의 체결 및 이행)
          
        - MBTI 커뮤니티 서비스 (동의 후 처리): MBTI 성향 결과(mbti), 문항별 선택 내역(UserSelect), 커뮤니티 작성 글·댓글, 투표 참여 내역(VoteUser)
          (근거: 「개인정보 보호법」 제15조 제1항 제1호 정보주체의 동의)
        
        2. 개인정보의 보유 및 파기 규칙
        - 보유 기간: 수집된 모든 사용자 데이터는 회원 탈퇴 시까지 보관됩니다.
        - 파기 방법: 사용자가 앱 내에서 회원 탈퇴를 요청하거나 프로젝트가 종료되는 경우, Supabase DB에서 관련 참조 데이터를 포함하여 즉시 영구 삭제(Cascade Delete) 처리합니다.
        
        3. 제3자 제공 및 위탁에 관한 사항
        본 앱은 학술 및 개발 실습 목적으로 운영되므로, 사용자의 개인정보를 외부 제3자에게 절대 제공하거나 마케팅 목적으로 위탁하지 않습니다.
        
        4. 이용자의 권리 행사 방법
        사용자는 언제든지 앱 내 탈퇴 기능이나 마이페이지(프로필 화면)를 통해 본인의 데이터를 직접 조회, 수정, 삭제할 수 있습니다.
        
        5. 개인정보 보호책임자 (문의처)
        프로젝트 관리자에게 개인정보 관련 고충 사항을 문의하실 수 있습니다.
        
        - 담당자 (개발팀): <본인 이름 또는 팀명 입력>
        - 연락처 (이메일): <학교 이메일 또는 개발용 이메일 입력>
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "개인정보 처리방침",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = privacyPolicyText,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                modifier = Modifier.verticalScroll(scrollState)
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun NotificationDialog(onDismiss: () -> Unit) {
    var isCommentAlarmOn by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "알림 설정",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "새 댓글 알림 받기", fontSize = 16.sp)
                Switch(
                    checked = isCommentAlarmOn,
                    onCheckedChange = { isCommentAlarmOn = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF9B12F0))
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun MbtiChangeDialog(
    currentMbti: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val mbtis = listOf(
        "ISTJ" to "전략가", "ISFJ" to "수호자", "INFJ" to "옹호자", "INTJ" to "전략가",
        "ISTP" to "장인", "ISFP" to "예술가", "INFP" to "중재자", "INTP" to "분석가",
        "ESTP" to "활동가", "ESFP" to "연예인", "ENFP" to "활동가", "ENTP" to "변론가",
        "ESTJ" to "경영자", "ESFJ" to "외교관", "ENFJ" to "선도자", "ENTJ" to "지도자"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "MBTI 변경", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                for (i in mbtis.indices step 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (j in 0 until 4) {
                            if (i + j < mbtis.size) {
                                val (mbtiName, nickname) = mbtis[i + j]

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSelect(mbtiName) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (mbtiName == currentMbti) Color(0xFF9B12F0) else Color(0xFFF3F4F6)
                                    ),
                                    elevation = CardDefaults.cardElevation(0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = mbtiName,
                                            color = if (mbtiName == currentMbti) Color.White else Color(0xFF374151),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = nickname,
                                            color = if (mbtiName == currentMbti) Color(0xFFE5E7EB) else Color(0xFF6B7280),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", fontWeight = FontWeight.Bold)
            }
        }
    )
}
