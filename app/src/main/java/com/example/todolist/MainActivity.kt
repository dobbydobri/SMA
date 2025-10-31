package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

data class TodoItem(
    val id: Long,
    val text: String
)

class TodoViewModel : ViewModel() {
    private val _items = mutableStateListOf<TodoItem>()
    val items: List<TodoItem> get() = _items

    fun add(text: String) {
        val t = text.trim()
        if (t.isEmpty()) return
        _items.add(0, TodoItem(id = System.currentTimeMillis(), text = t))
    }

    fun update(id: Long, newText: String) {
        val idx = _items.indexOfFirst { it.id == id }
        if (idx != -1) _items[idx] = _items[idx].copy(text = newText.trim())
    }

    fun delete(id: Long) {
        _items.removeAll { it.id == id }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    TodoScreen()
                }
            }
        }
    }
}

@Composable
fun TodoScreen(vm: TodoViewModel = viewModel()) {
    var input by rememberSaveable { mutableStateOf("") }

    var editing by remember { mutableStateOf<TodoItem?>(null) }
    var editText by rememberSaveable { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Add a task") }
            )
            Button(
                onClick = {
                    vm.add(input)
                    input = ""
                },
                enabled = input.isNotBlank()
            ) { Text("Add") }
        }

        Spacer(Modifier.height(16.dp))

        // List
        if (vm.items.isEmpty()) {
            Text("List Empty")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vm.items, key = { it.id }) { item ->
                    TodoRow(
                        item = item,
                        onEdit = {
                            editing = item
                            editText = item.text
                        },
                        onDone = { vm.delete(item.id) }
                    )
                }
            }
        }
    }

    if (editing != null) {
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Edit task") },
            text = {
                TextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.update(editing!!.id, editText)
                        editing = null
                    },
                    enabled = editText.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TodoRow(
    item: TodoItem,
    onEdit: () -> Unit,
    onDone: () -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = item.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            TextButton(onClick = onDone) { Text("Done") }
            OutlinedButton(onClick = onEdit) { Text("Edit") }
        }
    }
}
