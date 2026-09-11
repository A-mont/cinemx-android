package com.cinemx.movies.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.cinemx.movies.core.ui.components.SheetCornerRadius
import com.cinemx.movies.core.ui.theme.brandGradient
import com.cinemx.movies.feature.auth.data.GoogleIdTokenProvider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val CONTENT_MAX_WIDTH = 420.dp

@OptIn(ExperimentalComposeUiApi::class)
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
    val keyboard = LocalSoftwareKeyboardController.current

    // Credential Manager necesita el contexto de la Activity para abrir el selector de cuentas.
    val googleProvider = remember { GoogleIdTokenProvider() }

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
            Surface(
                // En tablets el formulario no debe estirarse a todo el ancho.
                modifier = Modifier.widthIn(max = CONTENT_MAX_WIDTH),
                shape = RoundedCornerShape(SheetCornerRadius),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Header()

                    EmailField(
                        value = state.email,
                        errorText = state.emailError?.asString(),
                        onValueChange = viewModel::onEmailChange,
                    )

                    Spacer(Modifier.height(8.dp))

                    PasswordField(
                        value = state.password,
                        errorText = state.passwordError?.asString(),
                        isVisible = state.isPasswordVisible,
                        onValueChange = viewModel::onPasswordChange,
                        onToggleVisibility = viewModel::onTogglePasswordVisibility,
                        onDone = {
                            keyboard?.hide()
                            viewModel.onSubmit()
                        },
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            keyboard?.hide()
                            viewModel.onSubmit()
                        },
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
                        onClick = {
                            scope.launch {
                                viewModel.onGoogleStarted()
                                googleProvider.requestIdToken(context)
                                    .onSuccess { viewModel.onGoogleToken(it.idToken, it.rawNonce) }
                                    .onFailure { viewModel.onGoogleFailed(it) }
                            }
                        },
                        enabled = state.isSubmitEnabled,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (state.isGoogleLoading) {
                            ButtonSpinner(color = MaterialTheme.colorScheme.primary)
                        } else {
                            Text(stringResource(R.string.login_google))
                        }
                    }
                }
            }
        }
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
