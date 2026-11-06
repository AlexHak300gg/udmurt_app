package org.udmurtlang.learn.feature.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.domain.model.MAX_DISPLAY_NAME_LENGTH
import org.udmurtlang.learn.core.designsystem.brandAccent
import org.udmurtlang.learn.core.designsystem.AdaptivePanes
import org.udmurtlang.learn.core.designsystem.ScrollableContent
import org.udmurtlang.learn.core.designsystem.WideContentWidth
import org.udmurtlang.learn.core.designsystem.currentWindowClass
import org.udmurtlang.learn.core.designsystem.isWide
import org.udmurtlang.learn.core.designsystem.ZcRed
import org.udmurtlang.learn.core.designsystem.ZigzagEdge
import org.udmurtlang.learn.core.designsystem.UdmOrnament
import org.udmurtlang.learn.core.designsystem.Ornament
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.offset
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.StarPattern
import org.udmurtlang.learn.core.designsystem.ZcWhite
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.core.designsystem.components.GhostButton

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val forgotCloudText = stringResource(R.string.auth_forgot_cloud)
    val forgotLocalText = stringResource(R.string.auth_forgot_local)
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AuthEffect.Authenticated -> onAuthenticated()
                is AuthEffect.ShowMessage -> {
                    val text = when (effect.text) {
                        "auth_forgot_cloud" -> forgotCloudText
                        else -> forgotLocalText
                    }
                    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val wide = currentWindowClass().isWide
    ScrollableContent(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .then(if (wide) Modifier.statusBarsPadding() else Modifier),
        maxWidth = WideContentWidth,
        contentPadding = PaddingValues(horizontal = if (wide) 24.dp else 0.dp),
        centerVertically = wide,
    ) {
        AdaptivePanes(
            sideBySide = wide,
            spacing = 0.dp,
            first = {
                Column {
                    // Красная шапка с орнаментом
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(if (wide) 28.dp else 0.dp))
                            .background(brandAccent(ZcRed)),
                    ) {
                        Ornament(
                            kind = UdmOrnament.STAR,
                            color = ZcWhite.copy(alpha = 0.18f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 20.dp, y = (-10).dp)
                                .size(110.dp),
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (wide) Modifier else Modifier.statusBarsPadding())
                                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 26.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.auth_title),
                                style = MaterialTheme.typography.headlineMedium,
                                color = ZcWhite,
                            )
                            Text(
                                text = stringResource(R.string.auth_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = ZcWhite.copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                    // Рваный край шапки, как в макете
                    if (!wide) ZigzagEdge(color = brandAccent(ZcRed), tooth = 22.dp, depth = 11.dp)
                }
            },
            second = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Сегментация Вход / Регистрация
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShapeCompat)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        AuthTab(
                            label = stringResource(R.string.auth_tab_login),
                            selected = !state.isRegisterMode,
                            onClick = { if (state.isRegisterMode) viewModel.onIntent(AuthIntent.ToggleMode) },
                            modifier = Modifier.weight(1f),
                        )
                        AuthTab(
                            label = stringResource(R.string.auth_tab_register),
                            selected = state.isRegisterMode,
                            onClick = { if (!state.isRegisterMode) viewModel.onIntent(AuthIntent.ToggleMode) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (state.isRegisterMode) {
                        AuthField(
                            value = state.name,
                            onValueChange = { viewModel.onIntent(AuthIntent.NameChanged(it)) },
                            placeholder = stringResource(R.string.auth_name),
                            icon = Icons.Filled.Person,
                            supportingText = {
                                Text("${state.name.codePointCount(0, state.name.length)} / $MAX_DISPLAY_NAME_LENGTH")
                            },
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    val emailInvalid = state.error == "auth_error_email"
                    AuthField(
                        value = state.email,
                        onValueChange = { viewModel.onIntent(AuthIntent.EmailChanged(it)) },
                        placeholder = stringResource(R.string.auth_email),
                        icon = Icons.Filled.Mail,
                        error = emailInvalid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AuthField(
                        value = state.password,
                        onValueChange = { viewModel.onIntent(AuthIntent.PasswordChanged(it)) },
                        placeholder = stringResource(R.string.auth_password),
                        icon = Icons.Filled.Key,
                        error = state.error == "auth_error_password",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailing = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff
                                    else Icons.Filled.Visibility,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    )

                    state.error?.let { key ->
                        Text(
                            text = when (key) {
                                "auth_error_email" -> stringResource(R.string.auth_error_email)
                                "auth_error_password" -> stringResource(R.string.auth_error_password)
                                "auth_error_name" -> stringResource(R.string.auth_error_name)
                                else -> key
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    GameButton(
                        text = stringResource(if (state.isRegisterMode) R.string.auth_register else R.string.auth_login),
                        onClick = { viewModel.onIntent(AuthIntent.Submit) },
                        enabled = !state.isLoading,
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    GhostButton(
                        text = stringResource(R.string.auth_guest),
                        onClick = { viewModel.onIntent(AuthIntent.ContinueAsGuest) },
                        containerColor = Color.Transparent,
                        borderColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                    )
                    TextButton(
                        onClick = { viewModel.onIntent(AuthIntent.ToggleMode) },
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text(
                            text = stringResource(
                                if (state.isRegisterMode) R.string.auth_switch_to_login
                                else R.string.auth_switch_to_register,
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                    TextButton(onClick = { viewModel.onIntent(AuthIntent.ForgotPassword) }) {
                        Text(stringResource(R.string.auth_forgot), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            },
        )
    }
}

private val CircleShapeCompat = RoundedCornerShape(999.dp)

@Composable
private fun AuthTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(5.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) MaterialTheme.colorScheme.inverseSurface else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 9.dp, horizontal = 10.dp),
        )
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    error: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailing: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    supportingText: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        enabled = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(16.dp),
        isError = error,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
        },
        trailingIcon = trailing,
        label = { Text(placeholder) },
        supportingText = supportingText,
        modifier = modifier.fillMaxWidth(),
    )
}
