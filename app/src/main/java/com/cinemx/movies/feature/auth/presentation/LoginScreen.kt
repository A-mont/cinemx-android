package com.cinemx.movies.feature.auth.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinemx.movies.R
import com.cinemx.movies.core.ui.components.BrandMark
import com.cinemx.movies.core.ui.components.SheetCornerRadius
import com.cinemx.movies.core.ui.theme.brandGradient
import com.cinemx.movies.feature.auth.data.GoogleIdTokenProvider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val CONTENT_MAX_WIDTH = 420.dp

private val LOGO_TILE_SIZE = 104.dp
private val LOGO_TILE_SHAPE = RoundedCornerShape(30.dp)
private val GOOGLE_ICON_SIZE = 20.dp

private const val CARD_ENTRY_DELAY_MS = 180
private const val CARD_ENTRY_DURATION_MS = 450
private val CARD_ENTRY_OFFSET = 40.dp

private const val LOGO_ENTRY_SCALE = 0.6f

private const val HALO_PERIOD_MS = 2400
private const val HALO_MAX_GROWTH = 0.18f
private const val HALO_MAX_ALPHA = 0.30f

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Credential Manager necesita el contexto de la Activity para abrir el selector de cuentas.
    val googleProvider = remember { GoogleIdTokenProvider() }

    var hasEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { hasEntered = true }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                LoginEvent.NavigateToHome -> onLoggedIn()
                is LoginEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // El degradado de marca ocupa el fondo; el Scaffold solo aporta el snackbar.
        containerColor = Color.Transparent,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brandGradient)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            BrandLogo(
                visible = hasEntered,
                modifier = Modifier.padding(bottom = 28.dp),
            )

            Surface(
                // En tablets el formulario no debe estirarse a todo el ancho.
                modifier = Modifier
                    .widthIn(max = CONTENT_MAX_WIDTH)
                    .cardEntry(visible = hasEntered),
                shape = RoundedCornerShape(SheetCornerRadius),
                color = MaterialTheme.colorScheme.surface,
            ) {
                LoginForm(
                    state = state,
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                    onSubmit = viewModel::onSubmit,
                    onGoogleClick = {
                        scope.launch {
                            viewModel.onGoogleStarted()
                            googleProvider.requestIdToken(context)
                                .onSuccess { viewModel.onGoogleToken(it.idToken, it.rawNonce) }
                                .onFailure { viewModel.onGoogleFailed(it) }
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LoginForm(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onGoogleClick: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val submit = {
        keyboard?.hide()
        onSubmit()
    }

    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Header()

        EmailField(
            value = state.email,
            errorText = state.emailError?.asString(),
            onValueChange = onEmailChange,
        )

        Spacer(Modifier.height(8.dp))

        PasswordField(
            value = state.password,
            errorText = state.passwordError?.asString(),
            isVisible = state.isPasswordVisible,
            onValueChange = onPasswordChange,
            onToggleVisibility = onTogglePasswordVisibility,
            onDone = submit,
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = submit,
            enabled = state.isSubmitEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isLoading) {
                ButtonSpinner(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(stringResource(R.string.login_action))
            }
        }

        OrDivider(modifier = Modifier.padding(vertical = 20.dp))

        OutlinedButton(
            onClick = onGoogleClick,
            enabled = state.isSubmitEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isGoogleLoading) {
                ButtonSpinner(color = MaterialTheme.colorScheme.primary)
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(GOOGLE_ICON_SIZE),
                )
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.login_google))
            }
        }
    }
}

@Composable
private fun Modifier.cardEntry(visible: Boolean): Modifier {
    val alpha = animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = CARD_ENTRY_DURATION_MS,
            delayMillis = CARD_ENTRY_DELAY_MS,
        ),
        label = "cardAlpha",
    )
    val offset = animateDpAsState(
        targetValue = if (visible) 0.dp else CARD_ENTRY_OFFSET,
        animationSpec = tween(
            durationMillis = CARD_ENTRY_DURATION_MS,
            delayMillis = CARD_ENTRY_DELAY_MS,
            easing = FastOutSlowInEasing,
        ),
        label = "cardOffset",
    )

    return graphicsLayer {
        this.alpha = alpha.value
        translationY = offset.value.toPx()
    }
}

@Composable
private fun BrandLogo(visible: Boolean, modifier: Modifier = Modifier) {
    val entryScale = animateFloatAsState(
        targetValue = if (visible) 1f else LOGO_ENTRY_SCALE,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "logoScale",
    )
    val entryAlpha = animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = CARD_ENTRY_DURATION_MS),
        label = "logoAlpha",
    )

    val transition = rememberInfiniteTransition(label = "logoHalo")
    val pulse = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = HALO_PERIOD_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "haloPulse",
    )

    Box(
        modifier = modifier.graphicsLayer {
            scaleX = entryScale.value
            scaleY = entryScale.value
            alpha = entryAlpha.value
        },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(LOGO_TILE_SIZE)
                .graphicsLayer {
                    val haloScale = 1f + HALO_MAX_GROWTH * pulse.value
                    scaleX = haloScale
                    scaleY = haloScale
                    alpha = HALO_MAX_ALPHA * (1f - pulse.value)
                }
                .background(Color.White, LOGO_TILE_SHAPE),
        )

        BrandMark(
            size = LOGO_TILE_SIZE,
            elevation = 16.dp,
            contentDescription = stringResource(R.string.login_logo),
        )
    }
}

@Composable
private fun Header() {
    Text(
        text = stringResource(R.string.login_title),
        style = MaterialTheme.typography.headlineLarge,
    )
    Text(
        text = stringResource(R.string.login_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
    )
}

@Composable
private fun EmailField(
    value: String,
    errorText: String?,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.login_email)) },
        singleLine = true,
        isError = errorText != null,
        supportingText = errorText?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PasswordField(
    value: String,
    errorText: String?,
    isVisible: Boolean,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onDone: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.login_password)) },
        singleLine = true,
        isError = errorText != null,
        supportingText = errorText?.let { { Text(it) } },
        visualTransformation = if (isVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (isVisible) {
                        Icons.Rounded.VisibilityOff
                    } else {
                        Icons.Rounded.Visibility
                    },
                    contentDescription = stringResource(
                        if (isVisible) R.string.login_hide_password else R.string.login_show_password,
                    ),
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ButtonSpinner(color: Color) {
    CircularProgressIndicator(
        modifier = Modifier.size(20.dp),
        strokeWidth = 2.dp,
        color = color,
    )
}

@Composable
private fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.login_or),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}
