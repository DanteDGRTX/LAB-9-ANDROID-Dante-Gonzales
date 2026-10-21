package com.dantegonzales.lab9android

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.PaddingValues
import kotlinx.coroutines.CancellationException

@Composable
fun ScreenPosts(navController: NavHostController, servicio: PostApiService) {
    val listaPosts: SnapshotStateList<PostModel> = remember { mutableStateListOf() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var retry by remember { mutableStateOf(0) }
    LaunchedEffect(retry) {
        isLoading = true
        errorMessage = null
        try {
            val listado = servicio.getUserPosts()
            Log.d("POSTS", "Posts recibidos: ${listado.size}")
            listaPosts.clear()
            listado.forEach { listaPosts.add(it) }
            isLoading = false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = "Error al cargar posts: ${e.message}"
            isLoading = false
            Log.e("POSTS", "Error loading posts", e)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.padding(16.dp))
                    Button(onClick = { retry++ }) {
                        Text("Reintentar")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listaPosts) { item ->
                    PostItem(item = item, onClick = { navController.navigate("postsVer/${item.id}") })
                }
            }
        }
    }
}

@Composable
fun PostItem(item: PostModel, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(text = item.id.toString(), modifier = Modifier.padding(end = 2.dp))
        Text(text = item.title, modifier = Modifier.weight(1f))
        IconButton(onClick = onClick) {
            Icon(Icons.Outlined.Search, contentDescription = "Ver post ${item.id}")
        }
    }
}
@Composable
fun ScreenPost(navController: NavHostController, servicio: PostApiService, id: Int) {
    var post by remember(id) { mutableStateOf<PostModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var retry by remember(id) { mutableStateOf(0) }
    LaunchedEffect(id, retry) {
        isLoading = true
        errorMessage = null
        try {
            val xpost = servicio.getUserPostById(id)
            Log.d("POST_DETAIL", "Post recibido: ${xpost.id}")
            if (xpost.body.isNotBlank()) {
                post = xpost
            } else {
                errorMessage = "El post no tiene contenido"
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = "Error al cargar post: ${e.message}"
            Log.e("POST_DETAIL", "Error loading post $id", e)
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
        } else if (post != null) {
            val p = post!!
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    PostDetailField(label = "id", value = p.id.toString())
                }
                item {
                    PostDetailField(label = "userId", value = p.userId.toString())
                }
                item {
                    PostDetailField(label = "title", value = p.title)
                }
                item {
                    PostDetailField(label = "body", value = p.body)
                }
                if (p.link != null && p.link.isNotBlank()) {
                    item {
                        PostDetailField(label = "Link", value = p.link)
                    }
                }
                if (p.commentCount != null) {
                    item {
                        PostDetailField(label = "Comentarios", value = p.commentCount.toString())
                    }
                }
            }
        }
    }
}

@Composable
fun PostDetailField(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            singleLine = label == "id" || label == "userId",
            maxLines = if (label == "id" || label == "userId") 1 else Int.MAX_VALUE
        )
    }
}





