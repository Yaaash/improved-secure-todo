package com.proton.securetasks;

import android.content.Context;
import android.support.annotation.NonNull;
import com.proton.securetasks.data.FakeTasksRemoteDataSource;
import com.proton.securetasks.data.source.TasksRepository;
import com.proton.securetasks.data.source.local.TasksLocalDataSource;
import com.proton.securetasks.data.source.local.ToDoDatabase;
import com.proton.securetasks.util.AppExecutors;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Enables injection of mock implementations for
 * {@link com.proton.securetasks.data.source.TasksDataSource} at compile time. This is useful for testing, since it allows us to use
 * a fake instance of the class to isolate the dependencies and run a test hermetically.
 */
public class Injection {

    public static TasksRepository provideTasksRepository(@NonNull Context context) {
        checkNotNull(context);
        ToDoDatabase database = ToDoDatabase.getInstance(context);
        return TasksRepository.getInstance(FakeTasksRemoteDataSource.getInstance(),
                TasksLocalDataSource.getInstance(new AppExecutors(),
                        database.taskDao()));
    }
}
