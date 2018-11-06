package com.proton.securetasks;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.support.annotation.Nullable;

import com.proton.securetasks.data.Task;
import com.proton.securetasks.data.source.TasksDataSource;
import com.proton.securetasks.data.source.TasksRepository;


/**
 * Abstract class for View Models that expose a single {@link com.proton.securetasks.data.Task}.
 */
public abstract class TaskViewModel extends BaseObservable
        implements TasksDataSource.GetTaskCallback {

    public final ObservableField<String> snackbarText = new ObservableField<>();

    public final ObservableField<String> title = new ObservableField<>();

    public final ObservableField<String> description = new ObservableField<>();

    private final ObservableField<Task> mTaskObservable = new ObservableField<>();

    private final TasksRepository mTasksRepository;

    private final Context mContext;

    private boolean mIsDataLoading;

    public TaskViewModel(Context context, TasksRepository tasksRepository) {
        mContext = context.getApplicationContext(); // Force use of Application Context.
        mTasksRepository = tasksRepository;

        // Exposed observables depend on the mTaskObservable observable:
        mTaskObservable.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                Task task = mTaskObservable.get();
                if (task != null) {
                    title.set(task.getTitle());
                    description.set(task.getDescription());
                } else {
                    title.set(mContext.getString(R.string.no_data));
                    description.set(mContext.getString(R.string.no_data_description));
                }
            }
        });
    }

    public void start(String taskId) {
        if (taskId != null) {
            mIsDataLoading = true;
            mTasksRepository.getTask(taskId, this);
        }
    }

    public void setTask(Task task) {
        mTaskObservable.set(task);
    }

    // "completed" is two-way bound, so in order to intercept the new value, use a @Bindable
    // annotation and process it in the setter.
    @Bindable
    public boolean getCompleted() {
        Task task = mTaskObservable.get();
        return task != null && task.isCompleted();
    }

    public void setCompleted(boolean completed) {
        if (mIsDataLoading) {
            return;
        }
        Task task = mTaskObservable.get();

        // Notify repository and user
        if (completed) {
            mTasksRepository.completeTask(task);
            snackbarText.set(mContext.getResources().getString(R.string.task_marked_complete));
        } else {
            mTasksRepository.activateTask(task);
            snackbarText.set(mContext.getResources().getString(R.string.task_marked_active));
        }
    }

    @Bindable
    public boolean isDataAvailable() {
        return mTaskObservable.get() != null;
    }

    @Bindable
    public boolean isDataLoading() {
        return mIsDataLoading;
    }

    // This could be an observable, but we save a call to Task.getTitleForList() if not needed.
    @Bindable
    public String getTitleForList() {
        if (mTaskObservable.get() == null) {
            return "No data";
        }
        return mTaskObservable.get().getTitleForList();
    }

    @Override
    public void onTaskLoaded(Task task) {
        mTaskObservable.set(task);
        mIsDataLoading = false;
        notifyChange(); // For the @Bindable properties
    }

    @Override
    public void onDataNotAvailable() {
        mTaskObservable.set(null);
        mIsDataLoading = false;
    }

    public void deleteTask() {
        if (mTaskObservable.get() != null) {
            mTasksRepository.deleteTask(mTaskObservable.get().getId());
        }
    }

    public void onRefresh() {
        if (mTaskObservable.get() != null) {
            start(mTaskObservable.get().getId());
        }
    }

    public String getSnackbarText() {
        return snackbarText.get();
    }

    @Nullable
    protected String getTaskId() {
        return mTaskObservable.get().getId();
    }
}
