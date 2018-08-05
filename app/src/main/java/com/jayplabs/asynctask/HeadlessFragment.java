package com.jayplabs.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import java.lang.ref.WeakReference;

// Headless Fragment to handle AsyncTask issue on screen rotation
public class HeadlessFragment extends Fragment {

    // Declare some sort of interface that your AsyncTask will use to communicate with the Activity
    public interface TaskListener {
        void onTaskStarted();
        void onTaskFinished(String result);
    }

    private LongRunningTask mTask;
    private TaskListener mTaskListener;
    private String result;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Try to use the Activity as a listener
        if (context instanceof TaskListener) {
            mTaskListener = (TaskListener) context;
        } else {
            // You can decide if you want to mandate that the Activity implements your callback interface
            // in which case you should throw an exception if it doesn't:
            throw new IllegalStateException("Parent Activity Must Implement TaskListener");
            // or you could just swallow it and allow a state where nobody is listening
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        if (mTaskListener != null) {
            // If the AsyncTask finished when we didn't have a listener we can
            // deliver the result here
            if (result != null) {
                mTaskListener.onTaskFinished(result);
                result = null;
            } else if (mTask != null && (mTask.getStatus() == Status.RUNNING)) {
                // If AsyncTask is still running, then we can share the progress to activity here
                // I'm just showing the prgress dialog whenever AsyncTask is running
                mTaskListener.onTaskStarted();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this Fragment so that it will not be destroyed when an orientation
        // change happens and we can keep our AsyncTask running
        // This is VERY IMPORTANT
        setRetainInstance(true);
    }

    /**
     * The Activity can call this when it wants to start the task
     */
    public void startTask() {
        mTask = new LongRunningTask(this);
        mTask.execute();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // If you want to cancel the async task when activity is rotated, you can do it here in
        // onDetach whenever fragment is detached

        // This is VERY important to avoid a memory leak (because mListener is really a reference to an Activity)
        // When the orientation change occurs, onDetach will be called and since the Activity is being destroyed
        // we don't want to keep any references to it
        // When the Activity is being re-created, onAttach will be called and we will get our listener back
        /*if (mTask != null && (mTask.getStatus() == Status.RUNNING)) {
            mTask.cancel(true);
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // We still have to cancel the task in onDestroy because if the user exits the app or
        // finishes the Activity, we don't want the task to keep running
        // Since we are retaining the Fragment, onDestroy won't be called for an orientation change
        // so this won't affect our ability to keep the task running when the user rotates the device

        mTaskListener = null;
    }

    private static class LongRunningTask extends AsyncTask<String, Integer, String> {

        // Since AsyncTask is an inner class of HeadlessFragment, AsyncTask will hold a implicit reference
        // to fragment.
        // To avoid the strong reference we will hold a weakreference to avoid memory leak if we don't
        // cancel the task when fragment is detached
        private final WeakReference<HeadlessFragment> mReference;

        private LongRunningTask(HeadlessFragment fragment) {
            mReference = new WeakReference<>(fragment);
        }


        @Override
        protected void onPreExecute() {
            if (mReference.get().mTaskListener != null) {
                mReference.get().mTaskListener.onTaskStarted();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            for (int i = 0; i < 10; i++) {
                Log.d("AsyncTaskExample", "AsyncTask is working: " + i);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return "All done!!!";
        }

        @Override
        protected void onPostExecute(String s) {
            if (mReference.get().mTaskListener != null) {
                mReference.get().mTaskListener.onTaskFinished(s);
            } else {
                // If the task finishes while the orientation change is happening and while
                // the Fragment is not attached to an Activity, our mListener might be null
                // If you need to make sure that the result eventually gets to the Activity
                // you could save the result here, then in onActivityCreated you can pass it back
                // to the Activity

                mReference.get().result = s;
            }
        }
    }
}
