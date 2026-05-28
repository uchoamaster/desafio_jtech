<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import { useTaskBoardStore, type TaskItem } from '@/stores/taskBoard'
import { useThemeStore } from '@/stores/theme'

const authStore = useAuthStore()
const router = useRouter()
const taskBoardStore = useTaskBoardStore()
const themeStore = useThemeStore()

const newListName = ref('')
const renameListName = ref('')
const newTaskTitle = ref('')
const newTaskNotes = ref('')
const editingTaskId = ref<string | null>(null)
const editingTaskTitle = ref('')
const editingTaskNotes = ref('')
const feedbackMessage = ref('')
const feedbackType = ref<'success' | 'warning' | 'info'>('info')
const creatingStarterLists = ref(false)

const currentList = computed(() => taskBoardStore.currentList)
const progressLabel = computed(() => `${taskBoardStore.completedTasks} de ${taskBoardStore.totalTasks} concluidas`)
const openTasks = computed(() => taskBoardStore.totalTasks - taskBoardStore.completedTasks)

watch(
  currentList,
  (list) => {
    renameListName.value = list?.name ?? ''
    cancelEditing()
  },
  { immediate: true },
)

function setFeedback(message: string, type: 'success' | 'warning' | 'info' = 'info') {
  feedbackMessage.value = message
  feedbackType.value = type
}

function clearFeedback() {
  feedbackMessage.value = ''
}

async function submitList() {
  const result = await taskBoardStore.createList(newListName.value)

  if (!result.ok) {
    setFeedback(result.error, 'warning')
    return
  }

  newListName.value = ''
  setFeedback('Lista criada com sucesso.', 'success')
}

async function renameCurrentList() {
  if (!currentList.value) {
    return
  }

  const result = await taskBoardStore.renameList(currentList.value.id, renameListName.value)

  if (!result.ok) {
    setFeedback(result.error, 'warning')
    return
  }

  setFeedback('Lista renomeada.', 'success')
}

async function removeCurrentList() {
  if (!currentList.value) {
    return
  }

  if (!window.confirm(`Excluir a lista "${currentList.value.name}"?`)) {
    return
  }

  const result = await taskBoardStore.removeList(currentList.value.id)

  if (!result.ok) {
    setFeedback(result.error, 'warning')
    return
  }

  setFeedback('Lista removida.', 'success')
}

async function submitTask() {
  if (!currentList.value) {
    return
  }

  const result = await taskBoardStore.createTask(currentList.value.id, newTaskTitle.value, newTaskNotes.value)

  if (!result.ok) {
    setFeedback(result.error, 'warning')
    return
  }

  newTaskTitle.value = ''
  newTaskNotes.value = ''
  setFeedback('Tarefa adicionada.', 'success')
}

function startEditing(task: TaskItem) {
  if (task.completed) {
    setFeedback('Conclua apenas o status. Para editar, reabra a tarefa primeiro.', 'warning')
    return
  }

  editingTaskId.value = task.id
  editingTaskTitle.value = task.title
  editingTaskNotes.value = task.notes
  clearFeedback()
}

function cancelEditing() {
  editingTaskId.value = null
  editingTaskTitle.value = ''
  editingTaskNotes.value = ''
}

async function saveTask(taskId: string) {
  if (!currentList.value) {
    return
  }

  const result = await taskBoardStore.updateTask(
    currentList.value.id,
    taskId,
    editingTaskTitle.value,
    editingTaskNotes.value,
  )

  if (!result.ok) {
    setFeedback(result.error, 'warning')
    return
  }

  cancelEditing()
  setFeedback('Tarefa atualizada.', 'success')
}

async function toggleTask(taskId: string, completed: boolean) {
  if (!currentList.value) {
    return
  }

  const result = await taskBoardStore.toggleTask(currentList.value.id, taskId, completed)

  if (!result.ok) {
    setFeedback(result.error, 'warning')
    return
  }

  clearFeedback()
}

async function deleteTask(taskId: string) {
  if (!currentList.value) {
    return
  }

  if (!window.confirm('Remover esta tarefa?')) {
    return
  }

  const result = await taskBoardStore.deleteTask(currentList.value.id, taskId)

  if (!result.ok) {
    setFeedback(result.error, 'warning')
    return
  }

  cancelEditing()
  setFeedback('Tarefa removida.', 'success')
}

async function createStarterLists() {
  creatingStarterLists.value = true
  const result = await taskBoardStore.createStarterLists()
  creatingStarterLists.value = false

  if (!result.ok) {
    setFeedback(result.error, 'warning')
    return
  }

  setFeedback('Workspace inicial criado com sucesso.', 'success')
}

function logout() {
  authStore.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <v-app>
    <v-app-bar app class="topbar" flat height="76" scroll-behavior="hide">
      <div class="topbar__inner">
        <div class="topbar__brand">
          <v-avatar class="topbar__avatar" color="primary" size="42">JT</v-avatar>
          <div>
            <p class="topbar__eyebrow">Workspace ativo</p>
            <strong>JTech Tasklist</strong>
          </div>
        </div>

        <div class="topbar__meta">
          <div class="topbar__pill">
            <span>{{ taskBoardStore.totalTasks }}</span>
            <small>tarefas</small>
          </div>
          <div class="topbar__pill">
            <span>{{ openTasks }}</span>
            <small>em aberto</small>
          </div>
          <v-btn class="theme-toggle" size="small" variant="text" @click="themeStore.toggleTheme">
            <i :class="themeStore.isDark ? 'bi bi-sun-fill' : 'bi bi-moon-stars-fill'" aria-hidden="true" />
          </v-btn>
          <v-btn color="secondary" variant="tonal" @click="logout">Sair</v-btn>
        </div>
      </div>
    </v-app-bar>

    <v-main class="dashboard-main">
      <v-container class="py-6 py-md-8" fluid>
        <div class="dashboard-shell">
          <v-card class="sidebar-card pa-4 pa-md-6">
            <div class="dashboard-toolbar mb-6">
              <v-chip color="primary" variant="flat">Task board</v-chip>
              <span class="toolbar-caption">organize por contexto</span>
            </div>

            <div class="d-flex align-center justify-space-between ga-4 mb-6">
              <div>
                <p class="text-overline text-secondary">Workspace</p>
                <h1 class="sidebar-title text-h4 font-weight-bold">{{ authStore.displayName }}</h1>
              </div>

              <v-avatar color="surface-bright" size="52">{{ authStore.displayName.slice(0, 2).toUpperCase() }}</v-avatar>
            </div>

            <v-sheet class="summary-panel mb-6 pa-4" color="surface-bright" rounded="xl">
              <p class="text-overline text-secondary">Resumo</p>
              <p class="text-h5 font-weight-bold mb-2">{{ progressLabel }}</p>
              <p class="text-body-2 text-medium-emphasis">
                Seus dados ficam persistidos por usuario no navegador.
              </p>
              <div class="summary-stats mt-4">
                <div>
                  <span class="summary-value">{{ taskBoardStore.lists.length }}</span>
                  <span class="summary-label">listas</span>
                </div>
                <div>
                  <span class="summary-value">{{ openTasks }}</span>
                  <span class="summary-label">pendentes</span>
                </div>
              </div>
            </v-sheet>

            <v-text-field
              v-model="newListName"
              class="mb-3"
              :disabled="taskBoardStore.syncing"
              hide-details
              label="Nova lista"
              placeholder="Ex: Marketing"
              @keyup.enter="submitList"
            />

            <v-btn :loading="taskBoardStore.syncing" block class="mb-6" color="primary" @click="submitList">Criar lista</v-btn>

            <TransitionGroup class="list-stack" name="list-motion" tag="div">
              <v-card
                v-for="list in taskBoardStore.lists"
                :key="list.id"
                :class="['list-card', { 'list-card--active': currentList?.id === list.id }]"
                color="surface"
                variant="tonal"
                @click="taskBoardStore.selectList(list.id)"
              >
                <v-card-text class="d-flex align-center justify-space-between ga-4 pa-4">
                  <div>
                    <p class="list-card__title text-body-1 font-weight-medium">{{ list.name }}</p>
                    <p class="list-card__meta text-caption">{{ list.tasks.length }} tarefas</p>
                  </div>

                  <v-chip class="list-card__counter" color="secondary" variant="flat">{{ list.tasks.filter((task) => task.completed).length }}</v-chip>
                </v-card-text>
              </v-card>
            </TransitionGroup>

            <v-sheet v-if="taskBoardStore.ready && !taskBoardStore.hasLists" class="sidebar-empty pa-5 mt-6" color="surface-bright" rounded="xl">
              <p class="text-overline text-secondary mb-2">Primeiro passo</p>
              <h3 class="text-h6 mb-2">Crie seu workspace inicial</h3>
              <p class="text-body-2 text-medium-emphasis mb-4">
                Você pode montar as listas manualmente ou subir um kit inicial com Trabalho, Estudos e Pessoal.
              </p>
              <v-btn :loading="creatingStarterLists" block color="secondary" variant="tonal" @click="createStarterLists">
                Gerar listas iniciais
              </v-btn>
            </v-sheet>
          </v-card>

          <v-card class="content-card pa-4 pa-md-6">
            <v-sheet v-if="taskBoardStore.loading" class="empty-state pa-8 text-center" color="surface-bright" rounded="xl">
              <p class="text-overline text-secondary mb-2">Sincronizando</p>
              <h2 class="text-h5 mb-2">Carregando dados da API</h2>
              <p class="text-medium-emphasis">Buscando listas e tarefas no backend.</p>
            </v-sheet>

            <div v-if="currentList">
              <div class="content-header mb-6">
                <div>
                  <p class="text-overline text-secondary">Lista ativa</p>
                  <h2 class="content-title text-h4 font-weight-bold">{{ currentList.name }}</h2>
                  <p class="section-lead mt-3">
                    Organize o fluxo do dia, marque entregas concluidas e mantenha cada contexto isolado.
                  </p>
                </div>

                <div class="content-header__stats">
                  <v-chip color="primary" size="large" variant="flat">{{ currentList.tasks.length }} itens</v-chip>
                  <v-chip color="secondary" size="large" variant="tonal">{{ currentList.tasks.filter((task) => task.completed).length }} concluidas</v-chip>
                </div>
              </div>

              <v-alert v-if="feedbackMessage" class="mb-4" :type="feedbackType" variant="tonal">
                {{ feedbackMessage }}
              </v-alert>

              <v-row class="mb-2" dense>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="renameListName"
                    :disabled="taskBoardStore.syncing"
                    hide-details
                    label="Renomear lista"
                    @keyup.enter="renameCurrentList"
                  />
                </v-col>

                <v-col class="d-flex align-center ga-2" cols="12" md="6">
                  <v-btn
                    aria-label="Renomear lista"
                    class="list-header__action"
                    :loading="taskBoardStore.syncing"
                    color="secondary"
                    icon
                    title="Renomear lista"
                    variant="tonal"
                    @click="renameCurrentList"
                  >
                    <i class="bi bi-pencil-square" aria-hidden="true" />
                  </v-btn>
                  <v-btn
                    aria-label="Excluir lista"
                    class="list-header__action list-header__action--danger"
                    :disabled="taskBoardStore.syncing"
                    color="error"
                    icon
                    title="Excluir lista"
                    variant="text"
                    @click="removeCurrentList"
                  >
                    <i class="bi bi-trash3" aria-hidden="true" />
                  </v-btn>
                </v-col>
              </v-row>

              <v-divider class="my-6" />

              <v-row class="composer-row" dense>
                <v-col cols="12" md="5">
                  <v-text-field
                    v-model="newTaskTitle"
                    :disabled="taskBoardStore.syncing"
                    hide-details
                    label="Nova tarefa"
                    placeholder="Ex: Revisar backlog"
                    @keyup.enter="submitTask"
                  />
                </v-col>

                <v-col cols="12" md="5">
                  <v-textarea
                    v-model="newTaskNotes"
                    :disabled="taskBoardStore.syncing"
                    auto-grow
                    hide-details
                    label="Observacoes"
                    rows="1"
                  />
                </v-col>

                <v-col class="d-flex align-center" cols="12" md="2">
                  <v-btn :loading="taskBoardStore.syncing" block color="primary" size="large" @click="submitTask">Adicionar</v-btn>
                </v-col>
              </v-row>

              <TransitionGroup v-if="currentList.tasks.length" class="task-grid mt-6" name="task-motion" tag="div">
                <v-card
                  v-for="task in currentList.tasks"
                  :key="task.id"
                  :class="['task-card', { 'task-card--done': task.completed }]"
                  color="surface"
                  variant="tonal"
                >
                  <v-card-text class="pa-4">
                    <div class="d-flex align-center justify-space-between ga-3 mb-3">
                      <v-chip :color="task.completed ? 'success' : 'primary'" size="small" variant="flat">
                        {{ task.completed ? 'Concluida' : 'Em aberto' }}
                      </v-chip>

                      <div class="d-flex ga-2 task-card__actions">
                        <v-btn
                          class="task-card__action"
                          :aria-label="task.completed ? 'Reabrir tarefa' : 'Concluir tarefa'"
                          :disabled="taskBoardStore.syncing"
                          :title="task.completed ? 'Reabrir tarefa' : 'Concluir tarefa'"
                          icon
                          size="small"
                          variant="text"
                          @click="toggleTask(task.id, !task.completed)"
                        >
                          <i :class="task.completed ? 'bi bi-arrow-counterclockwise' : 'bi bi-check2-circle'" aria-hidden="true" />
                        </v-btn>
                        <v-btn
                          aria-label="Editar tarefa"
                          class="task-card__action"
                          :disabled="taskBoardStore.syncing"
                          icon
                          size="small"
                          title="Editar tarefa"
                          variant="text"
                          @click="startEditing(task)"
                        >
                          <i class="bi bi-pencil-square" aria-hidden="true" />
                        </v-btn>
                        <v-btn
                          aria-label="Excluir tarefa"
                          class="task-card__action task-card__action--danger"
                          :disabled="taskBoardStore.syncing"
                          color="error"
                          icon
                          size="small"
                          title="Excluir tarefa"
                          variant="text"
                          @click="deleteTask(task.id)"
                        >
                          <i class="bi bi-trash3" aria-hidden="true" />
                        </v-btn>
                      </div>
                    </div>

                    <div v-if="editingTaskId === task.id" class="task-card__editor">
                      <v-text-field
                        v-model="editingTaskTitle"
                        :disabled="taskBoardStore.syncing"
                        class="mb-3 task-card__editor-field"
                        hide-details
                        label="Titulo"
                        @keyup.enter="saveTask(task.id)"
                      />
                      <v-textarea
                        v-model="editingTaskNotes"
                        :disabled="taskBoardStore.syncing"
                        auto-grow
                        class="mb-3 task-card__editor-field"
                        hide-details
                        label="Observacoes"
                        rows="2"
                      />

                      <div class="d-flex justify-end ga-2 task-card__editor-actions">
                        <v-btn class="task-card__editor-cancel" :disabled="taskBoardStore.syncing" variant="text" @click="cancelEditing">Cancelar</v-btn>
                        <v-btn :loading="taskBoardStore.syncing" color="secondary" variant="tonal" @click="saveTask(task.id)">Salvar</v-btn>
                      </div>
                    </div>

                    <div v-else>
                      <h3 class="task-card__title text-h6 mb-2">{{ task.title }}</h3>
                      <p class="task-card__notes text-body-2">
                        {{ task.notes || 'Sem observacoes adicionais.' }}
                      </p>
                    </div>
                  </v-card-text>
                </v-card>
              </TransitionGroup>

              <v-sheet v-else class="empty-state mt-6 pa-8 text-center" color="surface-bright" rounded="xl">
                <p class="text-overline text-secondary mb-2">Lista vazia</p>
                <h3 class="text-h5 mb-2">Transforme essa lista em um sprint claro</h3>
                <p class="text-medium-emphasis mb-4">
                  Crie a primeira tarefa desta lista para ativar o acompanhamento real pelo backend.
                </p>
                <v-btn color="primary" variant="flat" @click="newTaskTitle = 'Primeira entrega do dia'">Usar sugestao de tarefa</v-btn>
              </v-sheet>
            </div>

            <v-sheet v-else-if="taskBoardStore.ready" class="empty-state pa-8 text-center" color="surface-bright" rounded="xl">
              <p class="text-overline text-secondary mb-2">Workspace vazio</p>
              <h2 class="text-h5 mb-2">Nenhuma lista disponivel</h2>
              <p class="text-medium-emphasis mb-4">Crie uma nova lista no painel lateral ou gere um kit inicial.</p>
              <v-btn :loading="creatingStarterLists" color="secondary" variant="tonal" @click="createStarterLists">Gerar listas iniciais</v-btn>
            </v-sheet>
          </v-card>
        </div>
      </v-container>
    </v-main>
  </v-app>
</template>

<style scoped>
.dashboard-shell {
  display: grid;
  gap: 1.5rem;
}

.topbar {
  border-bottom: 1px solid var(--page-border);
  background: var(--page-topbar) !important;
  backdrop-filter: blur(16px);
}

.topbar__inner {
  display: flex;
  width: min(100%, 1400px);
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin: 0 auto;
  padding: 0 1rem;
}

.topbar__brand,
.topbar__meta {
  display: flex;
  align-items: center;
  gap: 0.9rem;
}

.topbar__avatar {
  box-shadow: var(--page-shadow-soft);
}

.topbar__eyebrow {
  color: var(--page-muted);
  font-size: 0.74rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.topbar__pill {
  display: flex;
  min-width: 5.5rem;
  flex-direction: column;
  border: 1px solid var(--page-border);
  border-radius: 1rem;
  padding: 0.5rem 0.8rem;
  background: rgba(255, 255, 255, 0.04);
}

.topbar__pill span {
  font-size: 1.05rem;
  font-weight: 700;
  line-height: 1;
}

.topbar__pill small {
  color: var(--page-muted);
  font-size: 0.72rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.dashboard-main {
  padding-top: 1rem;
}

.dashboard-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toolbar-caption {
  color: var(--page-muted);
  font-size: 0.8rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.sidebar-card,
.content-card {
  border: 1px solid var(--page-border);
  background: var(--page-panel);
  box-shadow: var(--page-shadow);
  backdrop-filter: blur(20px);
  animation: pane-rise 0.45s ease both;
}

.sidebar-card {
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--page-highlight-soft) 30%, transparent), transparent 22%),
    var(--page-panel);
}

.content-card {
  animation-delay: 0.08s;
}

.sidebar-title,
.content-title {
  letter-spacing: -0.03em;
}

.summary-panel {
  border: 1px solid var(--page-border);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.summary-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
}

.summary-stats > div {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  padding: 0.85rem;
  border-radius: 1rem;
  background: rgba(255, 255, 255, 0.05);
}

.sidebar-empty {
  border: 1px dashed var(--page-border);
}

.summary-value {
  font-size: 1.35rem;
  font-weight: 700;
}

.summary-label {
  color: var(--page-muted);
  font-size: 0.82rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.list-stack,
.task-grid {
  display: grid;
  gap: 0.85rem;
}

.list-card {
  cursor: pointer;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    background-color 0.2s ease,
    box-shadow 0.2s ease;
  border: 1px solid transparent;
  box-shadow: none;
}

.list-card__title {
  color: var(--page-ink);
  line-height: 1.2;
}

.list-card__meta {
  color: color-mix(in srgb, var(--page-ink) 70%, var(--page-muted));
}

.list-card__counter {
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--page-border) 70%, transparent);
}

.list-card:hover,
.list-card--active {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--page-highlight) 50%, transparent);
  box-shadow: var(--page-shadow-soft);
}

.list-card:hover .list-card__title,
.list-card--active .list-card__title {
  color: color-mix(in srgb, var(--page-ink) 82%, var(--page-highlight) 18%);
}

.list-card:hover .list-card__meta,
.list-card--active .list-card__meta {
  color: color-mix(in srgb, var(--page-ink) 58%, var(--page-muted));
}

.content-header {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
}

.content-header__stats {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  align-items: center;
}

.list-header__action {
  color: color-mix(in srgb, var(--page-ink) 60%, var(--page-muted)) !important;
}

.list-header__action--danger {
  color: color-mix(in srgb, #d8584c 78%, var(--page-ink)) !important;
}

.composer-row {
  border-radius: 1.5rem;
  padding: 0.4rem;
  background: color-mix(in srgb, var(--page-panel-strong) 70%, transparent);
}

.task-card {
  border: 1px solid var(--page-border);
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.task-card__title {
  color: var(--page-ink);
  line-height: 1.25;
}

.task-card__notes {
  color: color-mix(in srgb, var(--page-ink) 66%, var(--page-muted));
}

.task-card__actions {
  flex-wrap: wrap;
}

.task-card__action {
  color: color-mix(in srgb, var(--page-ink) 58%, var(--page-muted)) !important;
  min-width: 2rem;
}

.task-card__action--danger {
  color: color-mix(in srgb, #d8584c 78%, var(--page-ink)) !important;
}

.task-card__editor-actions {
  align-items: center;
}

.task-card__editor-cancel {
  color: color-mix(in srgb, var(--page-ink) 62%, var(--page-muted)) !important;
}

.task-card__editor-field :deep(.v-field) {
  background: color-mix(in srgb, var(--page-panel) 90%, white 10%);
  color: var(--page-ink);
}

.task-card__editor-field :deep(.v-field__outline) {
  --v-field-border-opacity: 1;
  color: color-mix(in srgb, var(--page-border) 65%, var(--page-ink));
}

.task-card__editor-field :deep(.v-label),
.task-card__editor-field :deep(input),
.task-card__editor-field :deep(textarea) {
  color: var(--page-ink) !important;
  opacity: 1;
}

.task-card__editor-field :deep(input::placeholder),
.task-card__editor-field :deep(textarea::placeholder) {
  color: color-mix(in srgb, var(--page-ink) 48%, var(--page-muted));
  opacity: 1;
}

.task-card__editor-field :deep(.v-field--disabled) {
  opacity: 0.7;
}

.task-card:hover {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--page-highlight) 40%, transparent);
  box-shadow: var(--page-shadow-soft);
}

.task-card:hover .task-card__title {
  color: color-mix(in srgb, var(--page-ink) 84%, var(--page-highlight) 16%);
}

.task-card:hover .task-card__notes {
  color: color-mix(in srgb, var(--page-ink) 74%, var(--page-muted));
}

.list-motion-enter-active,
.list-motion-leave-active,
.task-motion-enter-active,
.task-motion-leave-active {
  transition:
    opacity 0.24s ease,
    transform 0.24s ease,
    filter 0.24s ease;
}

.list-motion-enter-from,
.list-motion-leave-to,
.task-motion-enter-from,
.task-motion-leave-to {
  opacity: 0;
  transform: translateY(14px) scale(0.98);
  filter: blur(6px);
}

.list-motion-move,
.task-motion-move {
  transition: transform 0.24s ease;
}

.task-card--done {
  background: color-mix(in srgb, var(--page-secondary-soft) 85%, transparent);
}

.empty-state {
  border: 1px dashed var(--page-border);
}

.section-lead {
  max-width: 42rem;
  color: var(--page-muted);
  font-size: 1rem;
  line-height: 1.65;
}

.theme-toggle {
  transition:
    transform 0.25s ease,
    background-color 0.25s ease;
}

.theme-toggle:hover {
  transform: rotate(12deg) scale(1.04);
}

@keyframes pane-rise {
  from {
    opacity: 0;
    transform: translateY(14px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (min-width: 960px) {
  .dashboard-shell {
    grid-template-columns: minmax(280px, 340px) 1fr;
    align-items: start;
  }

  .sidebar-card {
    position: sticky;
    top: 6.75rem;
  }

  .task-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 959px) {
  .topbar__inner {
    padding: 0 0.85rem;
  }

  .topbar__meta {
    gap: 0.5rem;
  }

  .topbar__pill {
    display: none;
  }

  .dashboard-main {
    padding-top: 0.75rem;
  }

  .content-header__stats {
    width: 100%;
  }
}
</style>