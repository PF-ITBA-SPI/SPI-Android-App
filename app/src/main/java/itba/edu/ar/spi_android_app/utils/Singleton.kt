package ar.edu.itba.spi_android_app.utils

/**
 * Singleton holder, provides a thread-safe way of creating singletons which require a parameter
 * (such as a [Context][android.content.Context]) to work properly.
 *
 * @see <a href="https://medium.com/@BladeCoder/kotlin-singletons-with-argument-194ef06edd9e">Source</a>
 */
open class SingletonHolder<out T, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    /**
     * Get the instance of this singleton, or initialize with `arg` if necessary.
     */
    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}
