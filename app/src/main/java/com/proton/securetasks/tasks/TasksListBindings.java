package com.proton.securetasks.tasks;

import android.databinding.BindingAdapter;
import android.widget.ListView;

import com.proton.securetasks.data.Task;

import java.util.List;

/**
 * Contains {@link BindingAdapter}s for the {@link Task} list.
 */
public class TasksListBindings {

    @SuppressWarnings("unchecked")
    @BindingAdapter("app:items")
    public static void setItems(ListView listView, List<Task> items) {
        TasksFragment.TasksAdapter adapter = (TasksFragment.TasksAdapter) listView.getAdapter();
        if (adapter != null)
        {
            adapter.replaceData(items);
        }
    }
}
