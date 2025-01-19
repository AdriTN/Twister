package com.grupo18.twister.navigation

object Routes {
    // Rutas sin parámetros
    const val WELCOME = "welcome"
    const val AUTH = "auth"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val SEARCH = "search"
    const val EDIT = "edit"
    const val SETTINGS = "settings"
    const val QR_SCANNER = "qr_scanner"

    // Rutas base (privadas) y sus composables con parámetros
    private const val ADD_QUESTION_BASE = "addQuestion"
    const val ADD_QUESTION = "$ADD_QUESTION_BASE/{twistId}"

    private const val MANAGE_QUESTIONS_BASE = "manageQuestions"
    const val MANAGE_QUESTIONS = "$MANAGE_QUESTIONS_BASE/{twist}"

    private const val TWIST_DETAIL_BASE = "twistDetail"
    const val TWIST_DETAIL = TWIST_DETAIL_BASE

    private const val PUBLIC_TWIST_BASE = "publicTwistDetail"
    const val PUBlIC_TWIST_DETAIL = "$PUBLIC_TWIST_BASE/{twistId}"

    private const val SOLO_TWIST_BASE = "soloTwist"
    const val SOLO_TWIST = SOLO_TWIST_BASE

    private const val GAME_SCREEN_BASE = "gameScreen"
    const val GAME_SCREEN = "$GAME_SCREEN_BASE/{twist}"

    private const val LIVE_TWIST_BASE = "liveTwist"
    const val LIVE_TWIST_SCREEN = "$LIVE_TWIST_BASE/{pin}"

    private const val FINAL_SCREEN_BASE = "finalScreen"
    const val FINAL_SCREEN = "$FINAL_SCREEN_BASE/{topPlayers}/{isAdmin}"

    //Navegación segura
    fun liveTwistRoute(pin: String): String = "$LIVE_TWIST_BASE/$pin"
}
