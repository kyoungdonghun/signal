#!/usr/bin/env python3
"""
Git Hook Manager

Programmatic interface for managing Git hooks.
Can be used directly from command line or imported as a module.

Usage:
    python3 hook_manager.py list
    python3 hook_manager.py install docs-validation
    python3 hook_manager.py disable pre-commit
    python3 hook_manager.py enable pre-commit
    python3 hook_manager.py remove pre-commit
"""

import os
import sys
import shutil
import stat
from pathlib import Path
from typing import Dict, List, Optional

class HookManager:
    """Manage Git hooks for the repository."""

    # Standard Git hook types
    HOOK_TYPES = [
        'applypatch-msg', 'pre-applypatch', 'post-applypatch',
        'pre-commit', 'pre-merge-commit', 'prepare-commit-msg',
        'commit-msg', 'post-commit', 'pre-rebase', 'post-checkout',
        'post-merge', 'pre-push', 'pre-receive', 'update',
        'proc-receive', 'post-receive', 'post-update',
        'reference-transaction', 'push-to-checkout', 'pre-auto-gc',
        'post-rewrite', 'sendemail-validate', 'fsmonitor-watchman',
        'p4-changelist', 'p4-prepare-changelist', 'p4-post-changelist',
        'p4-pre-submit', 'post-index-change'
    ]

    # Template mapping: template_name -> (hook_type, template_file)
    TEMPLATES = {
        'docs-validation': ('pre-commit', 'pre-commit-docs-validation.sh'),
        'test-runner-commit': ('pre-commit', 'pre-commit-test-runner.sh'),
        'test-runner-push': ('pre-push', 'pre-push-test-runner.sh'),
        'commit-msg-format': ('commit-msg', 'commit-msg-format.sh'),
    }

    def __init__(self, repo_root: Optional[str] = None):
        """
        Initialize hook manager.

        Args:
            repo_root: Path to repository root. If None, searches from cwd.
        """
        if repo_root:
            self.repo_root = Path(repo_root)
        else:
            self.repo_root = self._find_repo_root()

        self.hooks_dir = self.repo_root / '.git' / 'hooks'
        self.skill_dir = self.repo_root / '.claude' / 'skills' / 'manage-hooks'
        self.templates_dir = self.skill_dir / 'assets'

        if not self.hooks_dir.exists():
            raise FileNotFoundError(f"Git hooks directory not found: {self.hooks_dir}")

    def _find_repo_root(self) -> Path:
        """Find repository root by looking for .git directory."""
        current = Path.cwd()
        while current != current.parent:
            if (current / '.git').exists():
                return current
            current = current.parent
        raise FileNotFoundError("Not in a git repository")

    def list_hooks(self) -> Dict[str, Dict[str, str]]:
        """
        List all hooks and their status.

        Returns:
            Dictionary mapping hook_type to status info:
            {
                'pre-commit': {'status': 'active', 'source': 'docs-validation'},
                'pre-push': {'status': 'disabled', 'source': 'unknown'},
                'commit-msg': {'status': 'none', 'source': '-'},
            }
        """
        hooks = {}

        for hook_type in self.HOOK_TYPES:
            hook_path = self.hooks_dir / hook_type
            disabled_path = self.hooks_dir / f"{hook_type}.disabled"

            if hook_path.exists() and os.access(hook_path, os.X_OK):
                # Active hook
                source = self._identify_source(hook_path)
                hooks[hook_type] = {'status': 'active', 'source': source}
            elif disabled_path.exists():
                # Disabled hook
                source = self._identify_source(disabled_path)
                hooks[hook_type] = {'status': 'disabled', 'source': source}
            elif hook_path.exists():
                # Exists but not executable (unusual)
                hooks[hook_type] = {'status': 'inactive', 'source': 'not executable'}
            # Don't list hooks that don't exist

        return hooks

    def _identify_source(self, hook_path: Path) -> str:
        """Identify hook source by checking content."""
        try:
            content = hook_path.read_text()

            # Check for known templates
            if 'Documentation changes detected' in content:
                return 'docs-validation'
            elif 'Running tests' in content:
                return 'test-runner'
            elif 'commit message' in content.lower():
                return 'commit-msg-format'
            else:
                return 'custom'
        except:
            return 'unknown'

    def install_template(self, template_name: str, force: bool = False) -> bool:
        """
        Install a hook from template.

        Args:
            template_name: Name of template (e.g., 'docs-validation')
            force: Overwrite existing hook without prompting

        Returns:
            True if successful, False otherwise
        """
        if template_name not in self.TEMPLATES:
            available = ', '.join(self.TEMPLATES.keys())
            print(f"❌ Unknown template: {template_name}")
            print(f"Available templates: {available}")
            return False

        hook_type, template_file = self.TEMPLATES[template_name]
        template_path = self.templates_dir / template_file
        hook_path = self.hooks_dir / hook_type

        if not template_path.exists():
            print(f"❌ Template file not found: {template_path}")
            return False

        # Check if hook already exists
        if hook_path.exists() and not force:
            print(f"⚠️  Hook already exists: {hook_path}")
            response = input("Overwrite? (y/N): ")
            if response.lower() != 'y':
                print("❌ Installation cancelled")
                return False

        # Copy template
        shutil.copy2(template_path, hook_path)

        # Make executable
        hook_path.chmod(hook_path.stat().st_mode | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH)

        print(f"✅ Installed: {template_name} → {hook_path}")
        print(f"✅ Made executable")

        return True

    def disable_hook(self, hook_type: str) -> bool:
        """
        Disable a hook by renaming it.

        Args:
            hook_type: Type of hook (e.g., 'pre-commit')

        Returns:
            True if successful, False otherwise
        """
        hook_path = self.hooks_dir / hook_type
        disabled_path = self.hooks_dir / f"{hook_type}.disabled"

        if not hook_path.exists():
            print(f"❌ Hook not found: {hook_type}")
            return False

        if disabled_path.exists():
            print(f"⚠️  Disabled version already exists: {disabled_path}")
            response = input("Overwrite? (y/N): ")
            if response.lower() != 'y':
                print("❌ Operation cancelled")
                return False
            disabled_path.unlink()

        hook_path.rename(disabled_path)
        print(f"✅ Disabled: {hook_type}")
        print(f"   {hook_path} → {disabled_path}")

        return True

    def enable_hook(self, hook_type: str) -> bool:
        """
        Enable a previously disabled hook.

        Args:
            hook_type: Type of hook (e.g., 'pre-commit')

        Returns:
            True if successful, False otherwise
        """
        hook_path = self.hooks_dir / hook_type
        disabled_path = self.hooks_dir / f"{hook_type}.disabled"

        if not disabled_path.exists():
            print(f"❌ Disabled hook not found: {hook_type}.disabled")
            return False

        if hook_path.exists():
            print(f"⚠️  Active hook already exists: {hook_path}")
            response = input("Overwrite? (y/N): ")
            if response.lower() != 'y':
                print("❌ Operation cancelled")
                return False
            hook_path.unlink()

        disabled_path.rename(hook_path)
        hook_path.chmod(hook_path.stat().st_mode | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH)

        print(f"✅ Enabled: {hook_type}")
        print(f"   {disabled_path} → {hook_path}")

        return True

    def remove_hook(self, hook_type: str, force: bool = False) -> bool:
        """
        Permanently remove a hook.

        Args:
            hook_type: Type of hook (e.g., 'pre-commit')
            force: Skip confirmation prompt

        Returns:
            True if successful, False otherwise
        """
        hook_path = self.hooks_dir / hook_type
        disabled_path = self.hooks_dir / f"{hook_type}.disabled"

        if not hook_path.exists() and not disabled_path.exists():
            print(f"❌ Hook not found: {hook_type}")
            return False

        if not force:
            print(f"⚠️  This will permanently delete the {hook_type} hook")
            response = input("Continue? (y/N): ")
            if response.lower() != 'y':
                print("❌ Operation cancelled")
                return False

        if hook_path.exists():
            hook_path.unlink()
            print(f"✅ Removed: {hook_path}")

        if disabled_path.exists():
            disabled_path.unlink()
            print(f"✅ Removed: {disabled_path}")

        return True


def main():
    """CLI entry point."""
    if len(sys.argv) < 2:
        print("Usage: hook_manager.py <command> [args]")
        print("\nCommands:")
        print("  list                    - List all hooks")
        print("  install <template>      - Install hook from template")
        print("  disable <hook-type>     - Disable a hook")
        print("  enable <hook-type>      - Enable a hook")
        print("  remove <hook-type>      - Remove a hook")
        sys.exit(1)

    command = sys.argv[1]
    manager = HookManager()

    if command == 'list':
        hooks = manager.list_hooks()
        if not hooks:
            print("No hooks installed")
        else:
            print("\nHook Type           | Status   | Source")
            print("--------------------|----------|-------------------")
            for hook_type, info in sorted(hooks.items()):
                print(f"{hook_type:19} | {info['status']:8} | {info['source']}")

    elif command == 'install':
        if len(sys.argv) < 3:
            print("❌ Missing template name")
            print(f"Available templates: {', '.join(HookManager.TEMPLATES.keys())}")
            sys.exit(1)
        template = sys.argv[2]
        success = manager.install_template(template)
        sys.exit(0 if success else 1)

    elif command == 'disable':
        if len(sys.argv) < 3:
            print("❌ Missing hook type")
            sys.exit(1)
        hook_type = sys.argv[2]
        success = manager.disable_hook(hook_type)
        sys.exit(0 if success else 1)

    elif command == 'enable':
        if len(sys.argv) < 3:
            print("❌ Missing hook type")
            sys.exit(1)
        hook_type = sys.argv[2]
        success = manager.enable_hook(hook_type)
        sys.exit(0 if success else 1)

    elif command == 'remove':
        if len(sys.argv) < 3:
            print("❌ Missing hook type")
            sys.exit(1)
        hook_type = sys.argv[2]
        success = manager.remove_hook(hook_type)
        sys.exit(0 if success else 1)

    else:
        print(f"❌ Unknown command: {command}")
        sys.exit(1)


if __name__ == '__main__':
    main()
