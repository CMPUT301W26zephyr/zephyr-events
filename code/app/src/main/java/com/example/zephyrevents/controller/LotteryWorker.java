package com.example.zephyrevents.controller;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.zephyrevents.repository.RepositoryCallback;

import java.util.concurrent.CountDownLatch;

public class LotteryWorker extends Worker {

    public LotteryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String eventId = getInputData().getString("eventId");
        if (eventId == null) return Result.failure();

        // Pause the background thread until Firebase finishes
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};

        LotteryController lotteryController = new LotteryController();
        lotteryController.runLottery(eventId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                success[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            return Result.retry();
        }

        return success[0] ? Result.success() : Result.retry();
    }
}