package com.proton.securetasks.statistics;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.VisibleForTesting;
import com.proton.securetasks.R;
import com.proton.securetasks.data.Task;
import com.proton.securetasks.data.source.TasksDataSource;
import com.proton.securetasks.data.source.TasksRepository;
import com.proton.securetasks.util.EspressoIdlingResource;


import java.util.List;

/**
 * Exposes the data to be used in the statistics screen.
 * <p>
 * This ViewModel uses both {@link ObservableField}s ({@link ObservableBoolean}s in this case) and
 * {@link Bindable} getters. The values in {@link ObservableField}s are used directly in the layout,
 * whereas the {@link Bindable} getters allow us to add some logic to it. This is
 * preferable to having logic in the XML layout.
 */
public class StatisticsViewModel extends BaseObservable {

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    final ObservableBoolean error = new ObservableBoolean(false);

    @VisibleForTesting
    int mNumberOfActiveTasks = 0;

    @VisibleForTesting
    int mNumberOfCompletedTasks = 0;

    private Context mContext;

    private final TasksRepository mTasksRepository;

    public StatisticsViewModel(Context context, TasksRepository tasksRepository) {
        mContext = context;
        mTasksRepository = tasksRepository;
    }

    public void start() {
        loadStatistics();
    }

    public void loadStatistics() {
        dataLoading.set(true);

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mTasksRepository.getTasks(new TasksDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }

                computeStats(tasks);
            }

            @Override
            public void onDataNotAvailable() {
                error.set(true);
            }
        });
    }
    /**
     * Returns a String showing the number of active tasks.
     */
    @Bindable
    public String getNumberOfActiveTasks() {
        return mContext.getString(R.string.statistics_active_tasks, mNumberOfActiveTasks);
    }

    /**
     * Returns a String showing the number of completed tasks.
     */
    @Bindable
    public String getNumberOfCompletedTasks() {
        return mContext.getString(R.string.statistics_completed_tasks, mNumberOfCompletedTasks);
    }

    /**
     * Controls whether the stats are shown or a "No data" message.
     */
    @Bindable
    public boolean isEmpty() {
        return mNumberOfActiveTasks + mNumberOfCompletedTasks == 0;
    }

    /**
     * Called when new data is ready.
     */
    private void computeStats(List<Task> tasks) {
        int completed = 0;
        int active = 0;

        for (Task task : tasks) {
            if (task.isCompleted()) {
                completed += 1;
            } else {
                active += 1;
            }
        }
        mNumberOfActiveTasks = active;
        mNumberOfCompletedTasks = completed;

        // There are multiple @Bindable fields in this ViewModel, calling notifyChange() will
        // update all the UI elements that depend on them.
        notifyChange();

        // To update just one of them and avoid unnecessary UI updates,
        // use notifyPropertyChanged(BR.field)

        // Observable fields don't need to be notified. set() will trigger an update.
        dataLoading.set(false);
        error.set(false);
    }
}
