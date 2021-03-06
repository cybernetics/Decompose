package com.arkivanov.todo.root.integration

import com.arkivanov.todo.edit.TodoEdit.Output as EditOutput
import com.arkivanov.todo.main.TodoMain.Input as MainInput

internal fun editOutputToListInput(id: Long): EditOutput.() -> MainInput? =
    {
        when (this) {
            is EditOutput.Changed -> MainInput.Update(id = id, text = text, isDone = isDone)
            is EditOutput.Finished -> null
        }
    }
