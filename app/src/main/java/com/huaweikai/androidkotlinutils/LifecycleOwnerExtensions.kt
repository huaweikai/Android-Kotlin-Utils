@file:Suppress("unused")

package com.huaweikai.androidkotlinutils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * created by william
 * @Date Created in 2023/5/22
 */
/**
 * 将block代码在重复在某个生命周期运行，每当LifecycleOwner进入该生命周期，block便执行一次
 * @param state 确定要在哪段周期执行
 */
fun LifecycleOwner.launchRepeatState(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    block: CoroutineScope.() -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(state) {
            this.block()
        }
    }
}

/**
 * 发起一个任务，在是该state时执行block，且只会执行一次
 */
fun LifecycleOwner.launchBlockWithStateOnce(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    block: () -> Unit
) {
    if (lifecycle.currentState.isAtLeast(state)) {
        block.invoke()
        return
    }
    lifecycleScope.launch {
        repeatOnLifecycle(state) {
            block.invoke()
            this@launch.cancel()
        }
    }
}

/**
 * 判断当前生命周期是否在Resumed
 */
val LifecycleOwner.isResumed: Boolean
    get() = this.lifecycle.currentState == Lifecycle.State.RESUMED

/**
 * 判断当前生命周期是否在Created
 */
val LifecycleOwner.isCreated: Boolean
    get() = this.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)

/**
 * 判断当前生命周期是否在Started
 */
val LifecycleOwner.isStarted: Boolean
    get() = this.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)


fun <T> Flow<T>.observe(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    block: (data: T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(state) {
            this@observe.collectLatest(block::invoke)
        }
    }
}

fun <T> Flow<T>.observe(
    lifecycleOwner: LifecycleOwner,
    block: (data: T) -> Unit
) {
    this.observe(lifecycleOwner, state = Lifecycle.State.STARTED, block = block::invoke)
}