package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

// Data Models
enum class Priority {
    LOW, MEDIUM, HIGH
}

data class Task(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val description: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ViewModel
class TaskViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun getTasks() {
        // في التطبيق الحقيقي، هنا سيتم جلب البيانات من قاعدة البيانات
        _tasks.value = listOf(
            Task(title = "تعلم Jetpack Compose", description = "إنشاء تطبيقات حديثة", priority = Priority.HIGH),
            Task(title = "تسوق البقالة", description = "شراء الفواكه والخضروات", priority = Priority.MEDIUM),
            Task(title = "ممارسة الرياضة", priority = Priority.LOW)
        )
    }

    fun addTask(task: Task) {
        _tasks.value = _tasks.value + task
    }

    fun updateTask(task: Task) {
        _tasks.value = _tasks.value.map { if (it.id == task.id) task else it }
    }

    fun deleteTask(task: Task) {
        _tasks.value = _tasks.value.filter { it.id != task.id }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

// Screens
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel = viewModel(),
    onAddTask: () -> Unit,
    onEditTask: (String) -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getTasks()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("قائمة المهام", fontSize = 20.sp) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Filled.Add, contentDescription = "إضافة مهمة")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // شريط البحث
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                label = { Text("ابحث في المهام") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "بحث") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد مهام، أضف مهمة جديدة!")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                it.description?.contains(searchQuery, ignoreCase = true) == true
                    }) { task ->
                        TaskItem(
                            task = task,
                            onTaskClick = { onEditTask(task.id.toString()) },
                            onCompleteTask = { viewModel.updateTask(task.copy(isCompleted = !task.isCompleted)) },
                            onDeleteTask = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onTaskClick: () -> Unit,
    onCompleteTask: () -> Unit,
    onDeleteTask: () -> Unit
) {
    Card(
        onClick = onTaskClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                task.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // إظهار الأولوية
                Text(
                    text = when (task.priority) {
                        Priority.HIGH -> "عالية"
                        Priority.MEDIUM -> "متوسطة"
                        Priority.LOW -> "منخفضة"
                    },
                    color = when (task.priority) {
                        Priority.HIGH -> MaterialTheme.colorScheme.error
                        Priority.MEDIUM -> MaterialTheme.colorScheme.primary
                        Priority.LOW -> MaterialTheme.colorScheme.secondary
                    },
                    fontSize = 12.sp
                )
            }

            Row {
                IconButton(onClick = onCompleteTask) {
                    Icon(
                        if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.Check,
                        contentDescription = if (task.isCompleted) "تم الإكمال" else "لم يكتمل",
                        tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDeleteTask) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذف")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    taskId: String?,
    viewModel: TaskViewModel = viewModel(),
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(TextFieldValue()) }
    var description by remember { mutableStateOf(TextFieldValue()) }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == null) "إضافة مهمة" else "تعديل المهمة", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان المهمة") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("وصف المهمة (اختياري)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("الأولوية:", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth()) {
                Priority.entries.forEach { p ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = {
                                Text(
                                    when (p) {
                                        Priority.HIGH -> "عالية"
                                        Priority.MEDIUM -> "متوسطة"
                                        Priority.LOW -> "منخفضة"
                                    }
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val task = Task(
                        id = if (taskId != null) UUID.fromString(taskId) else UUID.randomUUID(),
                        title = title.text,
                        description = description.text.ifEmpty { null },
                        priority = priority
                    )

                    if (taskId == null) {
                        viewModel.addTask(task)
                    } else {
                        viewModel.updateTask(task)
                    }
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(if (taskId == null) "إضافة المهمة" else "تعديل المهمة", fontSize = 16.sp)
            }
        }
    }
}

// Navigation
@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "task_list"
    ) {
        composable("task_list") {
            TaskListScreen(
                onAddTask = {
                    navController.navigate("add_edit_task")
                },
                onEditTask = { taskId ->
                    navController.navigate("add_edit_task/$taskId")
                }
            )
        }

        composable("add_edit_task") {
            AddEditTaskScreen(
                taskId = null,
                onBack = { navController.popBackStack() }
            )
        }

        composable("add_edit_task/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            AddEditTaskScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// Main Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // تصميم بسيط للتطبيق
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = androidx.compose.ui.graphics.Color(0xFF6200EE),
                    secondary = androidx.compose.ui.graphics.Color(0xFF03DAC6),
                    tertiary = androidx.compose.ui.graphics.Color(0xFF018786)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    SetupNavGraph(navController = navController)
                }
            }
        }
    }
}