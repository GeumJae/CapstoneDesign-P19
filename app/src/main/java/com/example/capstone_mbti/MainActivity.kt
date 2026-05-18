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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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

data class Post(
    val id: Int,
    val mbti: String,
    val author: String,
    val category: String,
    val title: String,
    val preview: String,
    val content: String,
    val likes: Int,
    val comments: Int,
    val badgeColor: Color,
    val badgeTextColor: Color
)

data class Comment(
    val nickname: String,
    val content: String
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
    val members: String,
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
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedFilter by remember { mutableStateOf("전체") }

    var myNickname by remember { mutableStateOf("ESTJ") }
    val myMbti = "ESTJ"
    val myTypeName = "경영자"

    val posts = remember {
        mutableStateListOf(
            Post(
                1,
                "ENFP",
                "ENFP_행복",
                "고민상담",
                "ENFP들만의 고민 있나요?",
                "저는 계획 세우는 게 너무 힘들어요ㅠㅠ",
                "저는 계획 세우는 게 너무 힘들어요ㅠㅠ 다들 이런 고민 있으신가요?",
                24,
                12,
                Color(0xFFE8FAD3),
                Color(0xFF58A51D)
            ),
            Post(
                2,
                "INTJ",
                "INTJ_전략",
                "질문",
                "INTJ vs INTP 뭐가 다를까요?",
                "둘 다 비슷해 보이는데 차이점이 궁금해요",
                "둘 다 비슷해 보이는데 차이점이 궁금해요. 실제로 어떤 점이 다른가요?",
                45,
                28,
                Color(0xFFF0DAFF),
                Color(0xFF9B12F0)
            ),
            Post(
                3,
                "ISFJ",
                "ISFJ_수호자",
                "일상",
                "오늘 좋은 일 있었어요!",
                "친구가 저한테 고마워한다고 했어요 😊",
                "오늘 친구가 저한테 고마워한다고 했어요 😊 괜히 기분 좋아졌네요.",
                67,
                15,
                Color(0xFFE2EEFF),
                Color(0xFF3B73E6)
            ),
            Post(
                4,
                "ESTP",
                "ESTP_모험",
                "투표",
                "주말에 뭐하고 놀까요?",
                "집에만 있기 너무 답답해요! 추천해주세요",
                "집에만 있기 너무 답답해요! 주말에 할 만한 거 추천해주세요.",
                32,
                19,
                Color(0xFFFFF3AF),
                Color(0xFFC48A00)
            ),
            Post(
                5,
                "INFP",
                "INFP_중재",
                "정보",
                "MBTI별 공부 스타일 정리",
                "유형별로 집중 잘 되는 방식이 조금 다른 것 같아요",
                "유형별로 집중 잘 되는 방식이 조금 다른 것 같아요. 저는 조용한 곳이 제일 좋았습니다.",
                18,
                7,
                Color(0xFFE0F7FA),
                Color(0xFF0096B7)
            ),
            Post(
                6,
                "ENTP",
                "ENTP_토론",
                "유머",
                "MBTI별 단톡방 특징ㅋㅋ",
                "E들은 계속 말하고 I들은 읽씹하는 느낌",
                "E들은 계속 말하고 I들은 읽씹하는 느낌이라 웃겼어요ㅋㅋ",
                51,
                22,
                Color(0xFFFFE4F2),
                Color(0xFFE13A8B)
            ),
            Post(
                7,
                "ESFJ",
                "ESFJ_친화",
                "연애",
                "연애할 때 F랑 T 차이 큰가요?",
                "공감 방식이 달라서 가끔 오해가 생기는 것 같아요",
                "공감 방식이 달라서 가끔 오해가 생기는 것 같아요. 다들 어떻게 생각하세요?",
                39,
                18,
                Color(0xFFFFE4E4),
                Color(0xFFD90014)
            ),
            Post(
                8,
                "ISTJ",
                "ISTJ_현실",
                "직장",
                "팀플할 때 제일 힘든 유형",
                "계획 안 지키는 사람이 제일 힘든 듯",
                "팀플할 때 계획 안 지키는 사람이 제일 힘든 듯합니다.",
                29,
                11,
                Color(0xFFE3E9FF),
                Color(0xFF4F46E5)
            ),
            Post(
                9,
                "ISFP",
                "ISFP_감성",
                "취미",
                "혼자 하기 좋은 취미 추천",
                "요즘 집에서 할 만한 취미 찾고 있어요",
                "요즘 집에서 할 만한 취미 찾고 있어요. 그림이나 악기 같은 것도 괜찮을까요?",
                21,
                9,
                Color(0xFFD7FAFB),
                Color(0xFF0F9BA8)
            )
        )
    }

    val commentsMap = remember {
        mutableStateMapOf<Int, MutableList<Comment>>(
            1 to mutableStateListOf(Comment("INTJ_전략", "저도 계획 세우는 거 힘들어요")),
            2 to mutableStateListOf(Comment("ENFP_행복", "INTJ는 계획형 느낌이 더 강한 듯")),
            3 to mutableStateListOf(Comment("ESTP_모험", "축하해요ㅋㅋ")),
            4 to mutableStateListOf(Comment("ISFJ_수호자", "카페 가는 것도 좋아요"))
        )
    }

    val shownPosts = when (selectedFilter) {
        "전체" -> posts
        "인기" -> posts.sortedByDescending { it.likes }
        else -> posts.filter { it.category == selectedFilter }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F8FA)
    ) {
        when (val screen = currentScreen) {
            is Screen.Home -> HomeScreen(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                posts = shownPosts,
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
                onSaveNickname = { myNickname = it },
                onHomeClick = {
                    selectedFilter = "전체"
                    currentScreen = Screen.Home
                },
                onCategoryClick = { currentScreen = Screen.Category },
                onProfileClick = { currentScreen = Screen.Profile }
            )

            is Screen.Detail -> DetailScreen(
                post = screen.post,
                comments = commentsMap.getOrPut(screen.post.id) { mutableStateListOf() },
                myNickname = myNickname,
                onBack = { currentScreen = Screen.Home },
                onDeleteClick = {
                    posts.removeAll { it.id == screen.post.id }
                    commentsMap.remove(screen.post.id)
                    currentScreen = Screen.Home
                },
                onAddComment = { newComment ->
                    commentsMap.getOrPut(screen.post.id) { mutableStateListOf() }.add(newComment)
                },
                onDeleteComment = { index ->
                    val list = commentsMap.getOrPut(screen.post.id) { mutableStateListOf() }
                    if (index in list.indices) list.removeAt(index)
                }
            )

            is Screen.Write -> WriteScreen(
                onBack = { currentScreen = Screen.Home },
                onSave = { mbti, author, category, title, content ->
                    val newPost = Post(
                        id = (posts.maxOfOrNull { it.id } ?: 0) + 1,
                        mbti = mbti,
                        author = author,
                        category = category,
                        title = title,
                        preview = content,
                        content = content,
                        likes = 0,
                        comments = 0,
                        badgeColor = Color(0xFFF0DAFF),
                        badgeTextColor = Color(0xFF9B12F0)
                    )
                    posts.add(0, newPost)
                    commentsMap[newPost.id] = mutableStateListOf()
                    selectedFilter = "전체"
                    currentScreen = Screen.Home
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    onWriteClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (posts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "게시글이 없습니다.",
                                fontSize = 18.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                } else {
                    itemsIndexed(posts) { _, post ->
                        HomePostItem(post = post, onClick = { onPostClick(post) })
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
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

            if (post.category == "투표") {
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
            Text("♡ ${post.likes}", fontSize = 17.sp, color = Color(0xFF6B7280))
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
        MbtiGroup("INTJ", "전략가", "234명 참여 중", Color(0xFF9B12F0)),
        MbtiGroup("ENFP", "활동가", "456명 참여 중", Color(0xFF4CAB00)),
        MbtiGroup("INFP", "중재자", "389명 참여 중", Color(0xFF0096B7)),
        MbtiGroup("ESTJ", "경영자", "312명 참여 중", Color(0xFFD90014))
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

                    groups.forEach {
                        MbtiGroupCard(group = it)
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
fun MbtiGroupCard(group: MbtiGroup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = group.members,
                    fontSize = 17.sp,
                    color = Color(0xFF374151)
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
    onSaveNickname: (String) -> Unit,
    onHomeClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    var editNickname by remember { mutableStateOf(nickname) }

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
                            text = "프로필",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "⚙",
                            fontSize = 34.sp,
                            color = Color(0xFF4B5563)
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFD90014)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📊", fontSize = 46.sp)
                        }

                        Spacer(modifier = Modifier.width(28.dp))

                        Column {
                            Text(
                                text = mbti,
                                fontSize = 30.sp,
                                color = Color(0xFF111827)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = typeName,
                                fontSize = 22.sp,
                                color = Color(0xFF4B5563)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(38.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        ProfileStat("24", "게시글", Modifier.weight(1f))
                        ProfileStat("156", "댓글", Modifier.weight(1f))
                        ProfileStat("432", "좋아요", Modifier.weight(1f))
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
                    ProfileMenuItem("내가 쓴 글")
                    ProfileMenuItem("내가 쓴 댓글")
                    ProfileMenuItem("좋아요 한 글")

                    Spacer(modifier = Modifier.height(28.dp))

                    Text("설정", fontSize = 18.sp, color = Color(0xFF374151))
                    Spacer(modifier = Modifier.height(14.dp))
                    ProfileMenuItem("알림 설정")
                    ProfileMenuItem("개인정보 처리방침")

                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
fun ProfileMenuItem(title: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
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
    comments: List<Comment>,
    myNickname: String,
    onBack: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddComment: (Comment) -> Unit,
    onDeleteComment: (Int) -> Unit
) {
    val context = LocalContext.current
    var commentInput by remember { mutableStateOf("") }

    BackHandler { onBack() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onBack) {
                    Text("뒤로가기", color = Color(0xFF9B12F0))
                }

                TextButton(
                    onClick = {
                        Toast.makeText(context, "게시글이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                        onDeleteClick()
                    }
                ) {
                    Text("삭제", color = Color(0xFFFF3B3B))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
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
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = post.title,
                        fontSize = 27.sp,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = post.content,
                        fontSize = 18.sp,
                        lineHeight = 27.sp,
                        color = Color(0xFF374151)
                    )
                }
            }
        }

        item {
            Text(
                text = "댓글",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        }

        if (comments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "아직 댓글이 없습니다.",
                        modifier = Modifier.padding(18.dp),
                        fontSize = 16.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        } else {
            itemsIndexed(comments) { index, comment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = comment.nickname,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )

                            TextButton(
                                onClick = {
                                    onDeleteComment(index)
                                    Toast.makeText(context, "댓글이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("삭제", color = Color(0xFFFF3B3B), fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = comment.content,
                            color = Color(0xFF374151),
                            fontSize = 16.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = commentInput,
                onValueChange = { commentInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("댓글을 입력하세요") },
                shape = RoundedCornerShape(14.dp)
            )
        }

        item {
            Button(
                onClick = {
                    if (commentInput.isBlank()) {
                        Toast.makeText(context, "댓글 내용을 입력하세요", Toast.LENGTH_SHORT).show()
                    } else {
                        onAddComment(Comment(myNickname, commentInput))
                        commentInput = ""
                        Toast.makeText(context, "댓글이 등록되었습니다", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9B12F0),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("댓글 등록", fontSize = 16.sp, modifier = Modifier.padding(vertical = 6.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    onBack: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    val context = LocalContext.current

    BackHandler { onBack() }

    var mbti by remember { mutableStateOf("INTJ") }
    var author by remember { mutableStateOf("INTJ_전략") }
    var category by remember { mutableStateOf("질문") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(22.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("뒤로가기", color = Color(0xFF9B12F0))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "글쓰기",
            fontSize = 30.sp,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(22.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip("INTJ", mbti == "INTJ") {
                mbti = "INTJ"
                author = "INTJ_전략"
            }
            FilterChip("ENFP", mbti == "ENFP") {
                mbti = "ENFP"
                author = "ENFP_행복"
            }
            FilterChip("ESTP", mbti == "ESTP") {
                mbti = "ESTP"
                author = "ESTP_모험"
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip("질문", category == "질문") { category = "질문" }
            FilterChip("일상", category == "일상") { category = "일상" }
            FilterChip("투표", category == "투표") { category = "투표" }
        }

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("제목") },
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            label = { Text("내용") },
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (title.isBlank() || content.isBlank()) {
                    Toast.makeText(context, "제목과 내용을 입력하세요", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "글이 등록되었습니다", Toast.LENGTH_SHORT).show()
                    onSave(mbti, author, category, title, content)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9B12F0),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("등록", fontSize = 17.sp, modifier = Modifier.padding(vertical = 7.dp))
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