import type { MenuNode } from '../types/system';

export interface TreeSelectNode {
  label: string;
  value: number;
  children?: TreeSelectNode[];
}

export function toTreeSelectData(menus: MenuNode[], excludeButtons = false): TreeSelectNode[] {
  return menus
    .filter((item) => !excludeButtons || item.type !== 'BUTTON')
    .map((item) => ({
      label: item.name,
      value: item.id,
      children: item.children ? toTreeSelectData(item.children, excludeButtons) : undefined
    }));
}

export function findAncestorKeys(menus: MenuNode[], targetPath: string): string[] {
  const result: string[] = [];

  function walk(nodes: MenuNode[], parents: string[]) {
    for (const node of nodes) {
      const key = node.path || String(node.id);
      if (node.path === targetPath) {
        result.push(...parents);
        return true;
      }
      if (node.children?.length) {
        if (walk(node.children, [...parents, key])) {
          return true;
        }
      }
    }
    return false;
  }

  walk(menus, []);
  return result;
}
