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
import android.content.Intent
import android.app.Activity

data class Post(
    val id: Int,
    val category: String,
    val title: String,
    val preview: String,
    val content: String
)

data class Comment(
    val nickname: String,
    val content: String
)

sealed class Screen {
    object Main : Screen()
    data class Detail(val post: Post) : Screen()
    object Write : Screen()
    object MyPage : Screen()
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
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
    var selectedCategory by remember { mutableStateOf("전체") }

    var myNickname by remember { mutableStateOf("(미정)") }
    val myMbti = "(미정)"

    val posts = remember {
        mutableStateListOf(
            Post(
                id = 1,
                category = "MBTI",
                title = "제목1",
                preview = "내용1",
                content = "내용1"
            ),
            Post(
                id = 2,
                category = "자유",
                title = "제목2",
                preview = "내용2",
                content = "내용2"
            ),
            Post(
                id = 3,
                category = "MBTI",
                title = "제목3",
                preview = "내용3",
                content = "내용3"
            ),
            Post(
                id = 4,
                category = "자유",
                title = "제목4",
                preview = "내용4",
                content = "내용4"
            )
        )
    }

    val commentsMap = remember {
        mutableStateMapOf(
            1 to mutableStateListOf(
                Comment("user1", "댓글1"),
                Comment("user2", "댓글2")
            ),
            2 to mutableStateListOf(
                Comment("user3", "댓글3")
            ),
            3 to mutableStateListOf(
                Comment("user4", "댓글4")
            ),
            4 to mutableStateListOf(
                Comment("user5", "댓글5")
            )
        )
    }

    val filteredPosts = posts.filter {
        selectedCategory == "전체" || it.category == selectedCategory
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F7)
    ) {
        when (val screen = currentScreen) {
            is Screen.Main -> MainScreen(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                posts = filteredPosts,
                onPostClick = { clickedPost ->
                    currentScreen = Screen.Detail(clickedPost)
                },
                onWriteClick = {
                    currentScreen = Screen.Write
                },
                onMyPageClick = {
                    currentScreen = Screen.MyPage
                }
            )

            is Screen.Detail -> DetailScreen(
                post = screen.post,
                comments = commentsMap.getOrPut(screen.post.id) { mutableStateListOf() },
                myNickname = myNickname,
                onBack = { currentScreen = Screen.Main },
                onDeleteClick = {
                    posts.removeAll { it.id == screen.post.id }
                    commentsMap.remove(screen.post.id)
                    currentScreen = Screen.Main
                },
                onAddComment = { newComment ->
                    commentsMap.getOrPut(screen.post.id) { mutableStateListOf() }.add(newComment)
                },
                onDeleteComment = { commentIndex ->
                    val list = commentsMap.getOrPut(screen.post.id) { mutableStateListOf() }
                    if (commentIndex in list.indices) {
                        list.removeAt(commentIndex)
                    }
                }
            )

            is Screen.Write -> WriteScreen(
                onBack = { currentScreen = Screen.Main },
                onSave = { category, title, content ->
                    val newPost = Post(
                        id = (posts.maxOfOrNull { it.id } ?: 0) + 1,
                        category = category,
                        title = title,
                        preview = content,
                        content = content
                    )
                    posts.add(0, newPost)
                    commentsMap[newPost.id] = mutableStateListOf()
                    currentScreen = Screen.Main
                }
            )

            is Screen.MyPage -> MyPageScreen(
                nickname = myNickname,
                mbti = myMbti,
                onBack = { currentScreen = Screen.Main },
                onSaveNickname = { newNickname ->
                    myNickname = newNickname
                }
            )
        }
    }
}

@Composable
fun MainScreen(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    onWriteClick: () -> Unit,
    onMyPageClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text = "MBTI",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1F1F)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CategoryChip(
                    text = "전체",
                    selected = selectedCategory == "전체",
                    onClick = { onCategorySelected("전체") }
                )
                CategoryChip(
                    text = "MBTI",
                    selected = selectedCategory == "MBTI",
                    onClick = { onCategorySelected("MBTI") }
                )
                CategoryChip(
                    text = "자유",
                    selected = selectedCategory == "자유",
                    onClick = { onCategorySelected("자유") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(posts) { _, post ->
                    PostCard(
                        post = post,
                        onClick = { onPostClick(post) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            BottomNavBar(
                selected = "홈",
                onHomeClick = { },
                onMyPageClick = onMyPageClick
            )
        }

        FloatingActionButton(
            onClick = onWriteClick,
            containerColor = Color(0xFF8D7BE7),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
        ) {
            Text(
                text = "+",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) Color(0xFF8D7BE7) else Color(0xFFE9E9EE)
    val textColor = if (selected) Color.White else Color(0xFF555555)

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(50),
        modifier = Modifier.height(42.dp)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PostCard(
    post: Post,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (post.category == "MBTI") {
                            Color(0xFF9FD8BE).copy(alpha = 0.25f)
                        } else {
                            Color(0xFFE4B583).copy(alpha = 0.25f)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = post.category,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202020)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.preview,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                lineHeight = 20.sp
            )
        }
    }
}

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

    BackHandler {
        onBack()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onBack) {
                    Text("뒤로가기")
                }

                TextButton(
                    onClick = {
                        Toast.makeText(context, "게시글이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                        onDeleteClick()
                    }
                ) {
                    Text("삭제", color = Color(0xFFD64B4B))
                }
            }
        }

        item {
            Text(
                text = post.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1F1F)
            )
        }

        item {
            Text(
                text = post.category,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = post.content,
                    modifier = Modifier.padding(18.dp),
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color(0xFF333333)
                )
            }
        }

        item {
            Text(
                text = "댓글",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (comments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "아직 댓글이 없습니다.",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
            }
        } else {
            itemsIndexed(comments) { index, comment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = comment.nickname,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )

                            TextButton(
                                onClick = {
                                    onDeleteComment(index)
                                    Toast.makeText(context, "댓글이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text(
                                    text = "삭제",
                                    color = Color(0xFFD64B4B),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = comment.content,
                            color = Color(0xFF444444),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Text(
                text = "댓글 작성",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
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
                        onAddComment(
                            Comment(
                                nickname = myNickname,
                                content = commentInput
                            )
                        )
                        commentInput = ""
                        Toast.makeText(context, "댓글이 등록되었습니다", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8D7BE7),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "댓글 등록",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    onBack: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    val context = LocalContext.current

    BackHandler {
        onBack()
    }

    var category by remember { mutableStateOf("MBTI") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("뒤로가기")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "글쓰기",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F1F1F)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CategoryChip(
                text = "MBTI",
                selected = category == "MBTI",
                onClick = { category = "MBTI" }
            )
            CategoryChip(
                text = "자유",
                selected = category == "자유",
                onClick = { category = "자유" }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                .height(220.dp),
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
                    onSave(category, title, content)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8D7BE7),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "등록",
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
fun MyPageScreen(
    nickname: String,
    mbti: String,
    onBack: () -> Unit,
    onSaveNickname: (String) -> Unit
) {
    val context = LocalContext.current

    BackHandler {
        onBack()
    }

    var isEditing by remember { mutableStateOf(false) }
    var editNickname by remember { mutableStateOf(nickname) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .padding(20.dp)
    ) {
        Text(
            text = "마이페이지",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F1F1F)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "사용자 정보",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = editNickname,
                        onValueChange = { editNickname = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("닉네임") },
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "MBTI: $mbti",
                        fontSize = 15.sp,
                        color = Color(0xFF555555)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val newNickname = if (editNickname.isBlank()) "(미정)" else editNickname
                            onSaveNickname(newNickname)
                            editNickname = newNickname
                            isEditing = false
                            Toast.makeText(context, "닉네임이 수정되었습니다", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8D7BE7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("저장")
                    }
                } else {
                    Text(
                        text = "닉네임: $nickname",
                        fontSize = 15.sp,
                        color = Color(0xFF555555)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "MBTI: $mbti",
                        fontSize = 15.sp,
                        color = Color(0xFF555555)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            editNickname = nickname
                            isEditing = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8D7BE7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("정보 수정")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                Toast.makeText(context, "MBTI 검사 페이지로 이동", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4F6EF7),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "MBTI 검사하기",
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD64B4B),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "로그아웃",
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        BottomNavBar(
            selected = "마이페이지",
            onHomeClick = onBack,
            onMyPageClick = { }
        )
    }
}

@Composable
fun BottomNavBar(
    selected: String,
    onHomeClick: () -> Unit,
    onMyPageClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "홈",
                fontSize = 16.sp,
                fontWeight = if (selected == "홈") FontWeight.Bold else FontWeight.Medium,
                color = if (selected == "홈") Color(0xFF8D7BE7) else Color.Gray,
                modifier = Modifier.clickable { onHomeClick() }
            )

            Text(
                text = "마이페이지",
                fontSize = 16.sp,
                fontWeight = if (selected == "마이페이지") FontWeight.Bold else FontWeight.Medium,
                color = if (selected == "마이페이지") Color(0xFF8D7BE7) else Color.Gray,
                modifier = Modifier.clickable { onMyPageClick() }
            )
        }
    }
}