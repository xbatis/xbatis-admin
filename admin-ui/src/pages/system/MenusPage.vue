<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import type { PageResponse } from '../../types/common';
import type { MenuFormPayload, MenuNode, MenuPageItem } from '../../types/system';
import { createMenu, deleteMenu, getMenuOptions, getMenuPage, updateMenu } from '../../api/system';
import { useAuthStore } from '../../stores/auth';
import { menuIconOptions } from '../../utils/icons';
import { toTreeSelectData } from '../../utils/tree';

const authStore = useAuthStore();
const formRef = ref<FormInstance>();
const loading = ref(false);
const saving = ref(false);
const modalOpen = ref(false);
const editingId = ref<number | null>(null);
const iconPickerOpen = ref(false);
const parentOptions = ref<MenuNode[]>([]);
const pageData = ref<PageResponse<MenuPageItem>>({
  records: [],
  current: 1,
  pageSize: 10,
  total: 0,
  totalPages: 0
});

const menuTypes = [
  { label: '目录', value: 'CATALOG' },
  { label: '菜单', value: 'MENU' },
  { label: '按钮', value: 'BUTTON' }
] as const;

const queryForm = reactive({
  keyword: '',
  type: undefined as MenuNode['type'] | undefined,
  status: undefined as number | undefined
});

const query = reactive({
  current: 1,
  pageSize: 10,
  keyword: '',
  type: undefined as MenuNode['type'] | undefined,
  status: undefined as number | undefined
});

const formModel = reactive<MenuFormPayload>({
  parentId: 0,
  name: '',
  type: 'MENU',
  path: '',
  component: '',
  icon: '',
  permission: '',
  redirect: '',
  sort: 0,
  visible: 1,
  keepAlive: 0,
  status: 1,
  remark: ''
});

function validatePath(_rule: unknown, value: string | undefined, callback: (error?: Error) => void) {
  const text = value?.trim();
  if (!text || text.startsWith('/')) {
    callback();
    return;
  }
  callback(new Error('路由路径需以 / 开头'));
}

function validateComponent(_rule: unknown, value: string | undefined, callback: (error?: Error) => void) {
  const text = value?.trim();
  if (!text || /^[A-Za-z0-9_/-]+$/.test(text)) {
    callback();
    return;
  }
  callback(new Error('组件标识只能包含字母、数字、下划线、中横线和 /'));
}

function validatePermission(_rule: unknown, value: string | undefined, callback: (error?: Error) => void) {
  const text = value?.trim();
  if (formModel.type === 'BUTTON' && !text) {
    callback(new Error('按钮权限必须填写权限码'));
    return;
  }
  if (!text || /^[A-Za-z0-9:_-]+$/.test(text)) {
    callback();
    return;
  }
  callback(new Error('权限码只能包含字母、数字、冒号、下划线和中横线'));
}

function validateIcon(_rule: unknown, value: string | undefined, callback: (error?: Error) => void) {
  const text = value?.trim();
  if (!text || menuIconOptions.some((item) => item.value === text)) {
    callback();
    return;
  }
  callback(new Error('请选择支持的菜单图标'));
}

const rules: FormRules<MenuFormPayload> = {
  name: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { min: 2, max: 32, message: '菜单名称长度需为 2-32 位', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  path: [
    { validator: validatePath, trigger: 'blur' },
    { max: 120, message: '路由路径不能超过 120 个字符', trigger: 'blur' }
  ],
  component: [
    { validator: validateComponent, trigger: 'blur' },
    { max: 120, message: '组件标识不能超过 120 个字符', trigger: 'blur' }
  ],
  icon: [{ validator: validateIcon, trigger: 'change' }],
  permission: [
    { validator: validatePermission, trigger: 'blur' },
    { max: 120, message: '权限码不能超过 120 个字符', trigger: 'blur' }
  ],
  redirect: [
    { validator: validatePath, trigger: 'blur' },
    { max: 120, message: '重定向不能超过 120 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序', trigger: 'change' },
    { type: 'number', min: 0, max: 9999, message: '排序范围为 0-9999', trigger: 'change' }
  ],
  remark: [{ max: 200, message: '备注不能超过 200 个字符', trigger: 'blur' }]
};

const canCreate = computed(() => authStore.hasPermission('sys:menu:create'));
const canEdit = computed(() => authStore.hasPermission('sys:menu:update'));
const canDelete = computed(() => authStore.hasPermission('sys:menu:delete'));
const hasActiveFilters = computed(
  () => Boolean(query.keyword.trim()) || query.type !== undefined || query.status !== undefined
);
const parentTreeData = computed(() => [
  { label: '根节点', value: 0 },
  ...toTreeSelectData(parentOptions.value, true)
]);
const selectedIconOption = computed(() => menuIconOptions.find((item) => item.value === formModel.icon));

const treeProps = {
  label: 'label',
  children: 'children'
};

const summary = computed(() => {
  const records = pageData.value.records;
  return [
    { label: hasActiveFilters.value ? '命中总数' : '菜单总数', value: pageData.value.total },
    { label: '当前页节点', value: records.length },
    { label: '当前页目录 / 菜单', value: records.filter((item) => item.type !== 'BUTTON').length },
    { label: '当前页启用', value: records.filter((item) => item.status === 1).length }
  ];
});

function typeLabel(type: MenuNode['type']) {
  return menuTypes.find((item) => item.value === type)?.label || type;
}

function resetFormModel() {
  Object.assign(formModel, {
    parentId: 0,
    name: '',
    type: 'MENU',
    path: '',
    component: '',
    icon: '',
    permission: '',
    redirect: '',
    sort: 0,
    visible: 1,
    keepAlive: 0,
    status: 1,
    remark: ''
  });
}

async function clearValidation() {
  await nextTick();
  formRef.value?.clearValidate();
}

async function loadData(refreshOptions = false) {
  loading.value = true;
  try {
    if (refreshOptions) {
      const [pageResult, optionData] = await Promise.all([getMenuPage(query), getMenuOptions()]);
      pageData.value = pageResult;
      parentOptions.value = optionData;
    } else {
      pageData.value = await getMenuPage(query);
    }
    query.current = pageData.value.current;
    query.pageSize = pageData.value.pageSize;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载菜单失败');
  } finally {
    loading.value = false;
  }
}

async function openCreate() {
  editingId.value = null;
  iconPickerOpen.value = false;
  resetFormModel();
  modalOpen.value = true;
  await clearValidation();
}

async function openEdit(record: MenuPageItem) {
  editingId.value = record.id;
  iconPickerOpen.value = false;
  Object.assign(formModel, {
    parentId: record.parentId ?? 0,
    name: record.name,
    type: record.type,
    path: record.path || '',
    component: record.component || '',
    icon: record.icon || '',
    permission: record.permission || '',
    redirect: record.redirect || '',
    sort: record.sort,
    visible: record.visible,
    keepAlive: record.keepAlive,
    status: record.status,
    remark: record.remark || ''
  });
  modalOpen.value = true;
  await clearValidation();
}

function closeDialog() {
  modalOpen.value = false;
}

async function selectIcon(value: string) {
  formModel.icon = value;
  iconPickerOpen.value = false;
  await nextTick();
  formRef.value?.validateField('icon');
}

async function clearIcon() {
  formModel.icon = '';
  await nextTick();
  formRef.value?.clearValidate('icon');
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  saving.value = true;
  const payload: MenuFormPayload = {
    ...formModel,
    name: formModel.name.trim(),
    path: formModel.path?.trim(),
    component: formModel.component?.trim(),
    icon: formModel.icon?.trim(),
    permission: formModel.permission?.trim(),
    redirect: formModel.redirect?.trim(),
    remark: formModel.remark?.trim()
  };
  try {
    if (editingId.value) {
      await updateMenu(editingId.value, payload);
      ElMessage.success('菜单更新成功');
    } else {
      await createMenu(payload);
      ElMessage.success('菜单创建成功');
    }
    modalOpen.value = false;
    await loadData(true);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function handleDelete(record: MenuPageItem) {
  try {
    await ElMessageBox.confirm(`确认删除菜单 ${record.name}？`, '确认操作', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    });
    await deleteMenu(record.id);
    ElMessage.success('菜单已删除');
    await loadData(true);
  } catch {
    // noop
  }
}

async function handleSearch() {
  query.keyword = queryForm.keyword.trim();
  query.type = queryForm.type;
  query.status = queryForm.status;
  query.current = 1;
  await loadData();
}

async function resetSearch() {
  queryForm.keyword = '';
  queryForm.type = undefined;
  queryForm.status = undefined;
  query.keyword = '';
  query.type = undefined;
  query.status = undefined;
  query.current = 1;
  await loadData();
}

async function handlePageChange(page: number) {
  query.current = page;
  await loadData();
}

async function handlePageSizeChange(pageSize: number) {
  query.pageSize = pageSize;
  query.current = 1;
  await loadData();
}

onMounted(() => {
  void loadData(true);
});
</script>

<template>
  <div class="page-stack">
    <section class="section-banner">
      <div>
        <p class="section-kicker">Menu Graph</p>
        <h3>菜单管理</h3>
        <p class="section-banner__copy">目录、页面路由与按钮权限都在这一层维护，支持按名称、路径和权限码检索，并按分页列表查看当前节点。</p>
      </div>
      <div class="section-banner__stats">
        <div v-for="item in summary" :key="item.label" class="mini-stat">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </div>
    </section>

    <section class="surface-panel">
      <div class="section-head">
        <div>
          <p class="section-kicker">Route & Action Nodes</p>
          <h3>菜单目录</h3>
        </div>
        <el-button v-if="canCreate" type="primary" @click="openCreate">新增菜单</el-button>
      </div>

      <div class="control-bar">
        <div class="query-bar">
          <el-input
            v-model="queryForm.keyword"
            clearable
            placeholder="名称 / 路径 / 权限码搜索"
            class="query-input"
            @keyup.enter="handleSearch"
          />
          <el-select v-model="queryForm.type" clearable placeholder="类型" class="query-select">
            <el-option v-for="item in menuTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="queryForm.status" clearable placeholder="状态" class="query-select">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </div>
        <div class="query-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>

      <el-table v-loading="loading" row-key="id" :data="pageData.records" class="data-table">
        <el-table-column label="名称" min-width="280">
          <template #default="{ row }">
            <div class="menu-node-cell" :style="{ paddingLeft: `${row.depth * 20}px` }">
              <div class="menu-node-cell__title">
                <span class="menu-node-cell__name">{{ row.name }}</span>
                <el-tag size="small" effect="plain" type="info">L{{ row.depth + 1 }}</el-tag>
              </div>
              <div class="menu-node-cell__meta">父级链：{{ row.parentPathLabel }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag type="primary" effect="plain">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="路径" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="menu-text-cell">{{ row.path || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="权限码" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="menu-text-cell">{{ row.permission || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="canEdit" link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="canDelete" link type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-foot">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.pageSize"
          layout="total, sizes, prev, pager, next"
          :total="pageData.total"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>
    </section>

    <el-dialog v-model="modalOpen" :title="editingId ? '编辑菜单' : '新增菜单'" width="760px" destroy-on-close>
      <el-form ref="formRef" :model="formModel" :rules="rules" label-position="top">
        <div class="form-grid form-grid-2">
          <el-form-item label="父级节点">
            <el-tree-select
              v-model="formModel.parentId"
              :data="parentTreeData"
              :props="treeProps"
              check-strictly
              default-expand-all
              clearable
            />
          </el-form-item>
          <el-form-item label="菜单类型" prop="type">
            <el-select v-model="formModel.type">
              <el-option v-for="item in menuTypes" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>

        <div class="form-grid form-grid-2">
          <el-form-item label="名称" prop="name">
            <el-input v-model="formModel.name" />
          </el-form-item>
          <el-form-item label="图标" prop="icon">
            <el-popover v-model:visible="iconPickerOpen" placement="bottom-start" :width="560" trigger="click">
              <template #reference>
                <div class="icon-picker-input" :class="{ 'is-active': iconPickerOpen }">
                  <div class="icon-picker-input__prefix">
                    <el-icon v-if="selectedIconOption"><component :is="selectedIconOption.component" /></el-icon>
                  </div>
                  <div class="icon-picker-input__value" :class="{ 'is-placeholder': !selectedIconOption }">
                    {{ selectedIconOption ? formModel.icon : '点击选择图标' }}
                  </div>
                  <button
                    v-if="formModel.icon"
                    type="button"
                    class="icon-picker-input__clear"
                    @click.stop="clearIcon"
                  >
                    ×
                  </button>
                </div>
              </template>

              <div class="icon-picker-grid">
                <button
                  v-for="item in menuIconOptions"
                  :key="item.value"
                  type="button"
                  class="icon-picker-grid__item"
                  :class="{ 'is-active': formModel.icon === item.value }"
                  @click="selectIcon(item.value)"
                >
                  <el-icon class="icon-picker-grid__icon"><component :is="item.component" /></el-icon>
                </button>
              </div>
            </el-popover>
          </el-form-item>
        </div>

        <div class="form-grid form-grid-2">
          <el-form-item label="路由路径" prop="path">
            <el-input v-model="formModel.path" placeholder="/system/users" />
          </el-form-item>
          <el-form-item label="组件标识" prop="component">
            <el-input v-model="formModel.component" placeholder="system/users" />
          </el-form-item>
        </div>

        <div class="form-grid form-grid-2">
          <el-form-item label="权限码" prop="permission">
            <el-input
              v-model="formModel.permission"
              :placeholder="formModel.type === 'BUTTON' ? 'sys:user:create' : '页面权限码，可选'"
            />
          </el-form-item>
          <el-form-item label="重定向" prop="redirect">
            <el-input v-model="formModel.redirect" placeholder="目录可配置默认跳转" />
          </el-form-item>
        </div>

        <div class="form-grid form-grid-3">
          <el-form-item label="排序" prop="sort">
            <el-input-number v-model="formModel.sort" :min="0" :max="9999" class="full-width" />
          </el-form-item>
          <el-form-item label="是否显示">
            <el-radio-group v-model="formModel.visible">
              <el-radio :value="1">显示</el-radio>
              <el-radio :value="0">隐藏</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="formModel.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>

        <el-form-item v-if="formModel.type === 'MENU'" label="页面缓存">
          <el-radio-group v-model="formModel.keepAlive">
            <el-radio :value="1">开启</el-radio>
            <el-radio :value="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="formModel.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeDialog">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.menu-node-cell {
  min-width: 0;
}

.menu-node-cell__title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.menu-node-cell__name {
  min-width: 0;
  font-weight: 700;
  color: var(--text-main);
  word-break: break-all;
}

.menu-node-cell__meta,
.menu-text-cell {
  color: var(--text-faint);
  font-size: 12px;
  line-height: 1.6;
  word-break: break-all;
}

.menu-node-cell__meta {
  margin-top: 4px;
}

.icon-picker-input {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 32px;
  padding: 0 11px;
  border-radius: 16px;
  border: 1px solid var(--el-border-color);
  background: var(--el-fill-color-blank);
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.icon-picker-input:hover,
.icon-picker-input.is-active {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

.icon-picker-input__prefix {
  display: grid;
  place-items: center;
  width: 18px;
  color: var(--text-subtle);
  flex-shrink: 0;
}

.icon-picker-input__value {
  min-width: 0;
  flex: 1;
  color: var(--text-main);
  font-size: 14px;
  line-height: 1;
}

.icon-picker-input__value.is-placeholder {
  color: var(--text-faint);
}

.icon-picker-input__clear {
  border: 0;
  background: transparent;
  color: var(--text-faint);
  font-size: 16px;
  line-height: 1;
  cursor: pointer;
  padding: 0;
}

.icon-picker-grid {
  display: grid;
  grid-template-columns: repeat(10, minmax(0, 1fr));
  gap: 8px;
  max-height: 180px;
  overflow: auto;
  padding: 2px;
}

.icon-picker-grid__item {
  display: grid;
  place-items: center;
  aspect-ratio: 1;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.82);
  color: var(--text-main);
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease,
    transform 0.2s ease;
}

.icon-picker-grid__item:hover {
  border-color: rgba(25, 103, 255, 0.28);
  background: rgba(25, 103, 255, 0.08);
  transform: translateY(-1px);
}

.icon-picker-grid__item.is-active {
  border-color: var(--brand);
  background: rgba(25, 103, 255, 0.12);
  color: var(--brand-deep);
}

.icon-picker-grid__icon {
  font-size: 16px;
}
</style>
