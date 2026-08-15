package rocks.drifthyena.minetricity.simulation

object LibraryLoader {
    var loaded = false
        private set

    fun load() {
        if (loaded) return

        val os = System.getProperty("os.name")
        var ext: String? = null

        if (os.contains("windows")) {
            ext = "dll"
        } else if (os.contains("linux")) {
            ext = "so"
        } else if (os.contains("mac") || os.contains("darwin")) {
            ext = "dylib"
        } else {
            throw UnsupportedOperationException("No native KLU library is built or supported for $os")
        }

        var arch = System.getProperty("os.arch")

        if (arch.contains("aarch64") || arch.contains("arm")) {
            arch = "arm64"
        } else {
            arch = "x64"
        }



        loaded = true
    }
}