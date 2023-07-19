package club.someoneice.smm

import java.util.function.Supplier


class FastSupplier<T>(private val t: T) : Supplier<T> {
    override fun get(): T = t
}