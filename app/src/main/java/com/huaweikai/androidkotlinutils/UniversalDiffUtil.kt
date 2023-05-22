@file:Suppress("unused")
package com.huaweikai.androidkotlinutils

import androidx.recyclerview.widget.DiffUtil
import java.lang.reflect.Field


@Target(AnnotationTarget.FIELD)
annotation class ItemsTheSame

@Target(AnnotationTarget.FIELD)
annotation class ContentsTheSame


/**
 * data class Book(
 *  @ItemsTheSame
 *  val id: Int,
 *  val name: String,
 *  val time: Long
 *  )
 *
 *class SampleListAdapter: ListAdapter<Book, ViewHolder>(Book::class.java.diffUtil) {
 *  override fun onBindViewHolder(holder: ViewHolder, position: Int) {}
 *  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {}
 * }
 */

/**
 * 通用的DiffUtil，省去大量的重复无用 DiffUtil 的代码
 * 在使用时，将实体类中需要进行对比的 item是否相同的 加上[ItemsTheSame]注解, 内容是否相同的加上 [ContentsTheSame]注解
 * 整个实体类没有加注解的情况下，将直接对比内存地址
 */
inline val <reified T : Any> Class<T>.diffUtil: DiffUtil.ItemCallback<T>
    get() {
        return object : DiffUtil.ItemCallback<T>() {
            var itemsTheSame: Field? = null
            var contentsTheSame: Field? = null

            init {
                for (declaredField in this@diffUtil.declaredFields) {
                    disposeItemsTheSame(declaredField)
                    disposeContentsTheSame(declaredField)
                }
            }

            private fun disposeItemsTheSame(field: Field) {
                if (field.getAnnotation(ItemsTheSame::class.java) != null) {
                    if (itemsTheSame != null) {
                        throw RuntimeException("一个类只能有一个ItemsTheSame")
                    }
                    field.isAccessible = true
                    itemsTheSame = field
                }
            }

            private fun disposeContentsTheSame(field: Field) {
                if (field.getAnnotation(ContentsTheSame::class.java) != null) {
                    if (contentsTheSame != null) {
                        throw RuntimeException("一个类只能有一个ContentsTheSame")
                    }
                    field.isAccessible = true
                    contentsTheSame = field
                }
            }

            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
                val itemsTheSame = this.itemsTheSame ?: return oldItem == newItem
                val oItem = itemsTheSame.get(oldItem)
                val nItem = itemsTheSame.get(newItem)
                return oItem == nItem
            }

            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
                val contentsTheSame = this.contentsTheSame ?: return oldItem == newItem
                val oItem = contentsTheSame.get(oldItem)
                val nItem = contentsTheSame.get(newItem)
                return oItem == nItem
            }
        }
    }
