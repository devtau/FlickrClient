package com.devtau.database.listeners

import com.devtau.rest.model.Image
/**
 * Как альтернатива RESTClientView, слой бд не использует единый интерфейс обратного вызова.
 * Вместо этого каждый метод DataSource, предполагающий ответ от бд получает индивидуальный объект слушателя
 * с единственным методом
 */
interface ImagesListener {
    fun processImages(list: List<Image>?)
}