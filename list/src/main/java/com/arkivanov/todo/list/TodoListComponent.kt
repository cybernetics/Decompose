package com.arkivanov.todo.list

import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.layout.Row
import androidx.ui.layout.RowScope.weight
import androidx.ui.layout.Spacer
import androidx.ui.layout.padding
import androidx.ui.layout.width
import androidx.ui.material.Checkbox
import androidx.ui.material.Divider
import androidx.ui.text.AnnotatedString
import androidx.ui.text.style.TextOverflow
import androidx.ui.unit.dp
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.bind
import com.arkivanov.todo.database.TodoDatabaseQueries
import com.arkivanov.todo.list.integration.ListStoreDatabase
import com.arkivanov.todo.list.integration.inputToIntent
import com.arkivanov.todo.list.store.ListStore.Intent
import com.arkivanov.todo.list.store.ListStoreFactory
import com.arkivanov.todo.router.ComposableComponent
import com.arkivanov.todo.utils.observableState
import com.badoo.reaktive.base.Consumer
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.mapNotNull

class TodoListComponent(
    storeFactory: StoreFactory,
    queries: TodoDatabaseQueries,
    lifecycle: Lifecycle,
    input: Observable<Input>,
    private val output: Consumer<Output>
) : ComposableComponent {

    private val store = ListStoreFactory(storeFactory, ListStoreDatabase(queries)).create()

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            input.mapNotNull(inputToIntent) bindTo store
        }

        lifecycle.doOnDestroy(store::dispose)
    }

    @Composable
    override fun content() {
        val state = store.observableState()

        LazyColumnItems(items = state.value.items, modifier = Modifier.weight(1F)) { item ->
            Row(modifier = Modifier.clickable(onClick = { output.onNext(Output.Selected(id = item.id)) }) + Modifier.padding(8.dp)) {
                Text(
                    text = AnnotatedString(item.text),
                    modifier = Modifier.weight(1F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                Checkbox(
                    checked = item.isDone,
                    onCheckedChange = { store.accept(Intent.SetDone(id = item.id, isDone = !item.isDone)) }
                )
            }
            Divider()
        }
    }

    sealed class Input {
        data class Put(val id: Long, val timestamp: Long, val text: String) : Input()
        data class Update(val id: Long, val text: String, val isDone: Boolean) : Input()
    }

    sealed class Output {
        data class Selected(val id: Long) : Output()
    }
}