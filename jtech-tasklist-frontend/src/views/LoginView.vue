<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'

const authStore = useAuthStore()
const router = useRouter()
const themeStore = useThemeStore()

const credentials = reactive({
  username: '',
  password: '',
})

const feedback = ref('')
const submitting = ref(false)

async function submitLogin() {
  submitting.value = true
  const result = await authStore.login(credentials.username, credentials.password)
  submitting.value = false

  if (!result.ok) {
    feedback.value = result.error
    return
  }

  feedback.value = ''
  router.push({ name: 'dashboard' })
}
</script>

<template>
  <v-app>
    <v-main class="login-view">
      <v-container class="fill-height py-10" fluid>
        <v-row align="center" class="fill-height" justify="center">
          <v-col cols="12" lg="10" xl="8">
            <v-card class="login-card overflow-hidden">
              <v-row no-gutters>
                <v-col class="hero-panel" cols="12" md="6">
                  <div class="hero-copy">
                    <div class="hero-actions mb-8">
                      <v-chip class="hero-chip" color="primary" variant="flat">JTech Tasklist</v-chip>
                      <v-btn class="theme-toggle" size="small" variant="text" @click="themeStore.toggleTheme">
                        <i :class="themeStore.isDark ? 'bi bi-sun-fill' : 'bi bi-moon-stars-fill'" aria-hidden="true" />
                      </v-btn>
                    </div>
                    <p class="hero-kicker">Desafio Fullstack2</p>
                    <h1>Task board multiusuario com sessao persistida.</h1>
                    <p>
                      Entre com qualquer usuario e senha nao vazios para abrir seu espaco de listas,
                      tarefas e progresso salvo no navegador.
                    </p>
                    <div class="hero-pill-row mt-8">
                      <span class="hero-pill">JWT real</span>
                      <span class="hero-pill">CRUD em API</span>
                      <span class="hero-pill">Dark mode persistido</span>
                    </div>

                    <div class="hero-metrics mt-8">
                      <div>
                        <strong>API</strong>
                        <span>H2 local</span>
                      </div>
                      <div>
                        <strong>JWT</strong>
                        <span>sessao segura</span>
                      </div>
                    </div>
                  </div>
                </v-col>

                <v-col cols="12" md="6">
                  <v-card-text class="form-panel pa-8 pa-md-10">
                    <div class="mb-8">
                      <p class="text-overline text-secondary">Acesso</p>
                      <h2 class="text-h4 font-weight-bold">Entrar</h2>
                      <p class="form-lead mt-2">Abra seu workspace com credenciais simples e comece a organizar tarefas em segundos.</p>
                    </div>

                    <v-alert v-if="feedback" class="mb-4" type="warning" variant="tonal">
                      {{ feedback }}
                    </v-alert>

                    <v-form @submit.prevent="submitLogin">
                      <v-text-field
                        v-model="credentials.username"
                        autocomplete="username"
                        label="Usuario"
                        placeholder="ex: angelo"
                      />

                      <v-text-field
                        v-model="credentials.password"
                        autocomplete="current-password"
                        label="Senha"
                        placeholder="qualquer valor nao vazio"
                        type="password"
                      />

                      <v-btn :loading="submitting" block class="mt-4" color="primary" size="large" type="submit">
                        Acessar workspace
                      </v-btn>

                      <p class="login-note mt-4">Ao entrar pela primeira vez, a conta local e criada automaticamente na API.</p>
                    </v-form>
                  </v-card-text>
                </v-col>
              </v-row>
            </v-card>
          </v-col>
        </v-row>
      </v-container>
    </v-main>
  </v-app>
</template>

<style scoped>
.login-view {
  display: flex;
  align-items: center;
}

.login-card {
  overflow: hidden;
  border: 1px solid var(--page-border);
  background: var(--page-panel);
  box-shadow: var(--page-shadow);
  backdrop-filter: blur(18px);
  animation: card-rise 0.55s ease both;
}

.hero-panel {
  display: flex;
  min-height: 420px;
  align-items: flex-end;
  padding: 2.5rem;
  background:
    radial-gradient(circle at top right, var(--page-highlight-soft), transparent 32%),
    linear-gradient(180deg, rgba(116, 213, 192, 0.08), transparent 45%),
    linear-gradient(145deg, rgba(255, 255, 255, 0.06), var(--page-panel-strong));
}

.hero-copy {
  max-width: 26rem;
}

.hero-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero-chip {
  letter-spacing: 0.04em;
  box-shadow: var(--page-shadow-soft);
}

.theme-toggle {
  transition:
    transform 0.25s ease,
    background-color 0.25s ease;
}

.theme-toggle:hover {
  transform: rotate(12deg) scale(1.04);
}

.hero-kicker {
  margin-bottom: 1rem;
  font-size: 0.85rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--page-highlight);
}

.hero-copy h1 {
  margin-bottom: 1rem;
  font-size: clamp(2.3rem, 4vw, 3.8rem);
  line-height: 0.95;
  letter-spacing: -0.04em;
}

.hero-copy p {
  max-width: 24rem;
  color: var(--page-muted);
}

.hero-pill-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.hero-pill {
  border: 1px solid var(--page-border);
  border-radius: 999px;
  padding: 0.55rem 0.95rem;
  font-size: 0.82rem;
  color: var(--page-ink);
  background: rgba(255, 255, 255, 0.04);
}

.hero-metrics {
  display: flex;
  gap: 1rem;
}

.hero-metrics > div {
  display: flex;
  min-width: 8rem;
  flex-direction: column;
  gap: 0.1rem;
  border-top: 1px solid var(--page-border);
  padding-top: 0.9rem;
}

.hero-metrics strong {
  font-size: 1.4rem;
  line-height: 1;
}

.hero-metrics span {
  color: var(--page-muted);
  font-size: 0.82rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.form-panel {
  display: flex;
  min-height: 100%;
  flex-direction: column;
  justify-content: center;
}

.form-lead {
  max-width: 28rem;
  color: var(--page-muted);
  font-size: 0.98rem;
}

.login-note {
  color: var(--page-muted);
  text-align: center;
  font-size: 0.92rem;
}

@keyframes card-rise {
  from {
    opacity: 0;
    transform: translateY(18px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 959px) {
  .hero-panel {
    min-height: 320px;
    padding: 2rem;
  }

  .hero-metrics {
    flex-wrap: wrap;
  }
}
</style>