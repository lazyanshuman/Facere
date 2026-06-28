CREATE TABLE IF NOT EXISTS app_settings (
    key        TEXT     NOT NULL PRIMARY KEY,
    value      TEXT,
    updated_at DATETIME DEFAULT (datetime('now'))
);

INSERT OR IGNORE INTO app_settings (key, value) VALUES ('last_opened_date', NULL);
INSERT OR IGNORE INTO app_settings (key, value) VALUES ('theme_mode', 'AUTO_SYSTEM');
INSERT OR IGNORE INTO app_settings (key, value) VALUES ('app_locked', 'false');
INSERT OR IGNORE INTO app_settings (key, value) VALUES ('biometric_enabled', 'false');
INSERT OR IGNORE INTO app_settings (key, value) VALUES ('lock_pin_hash', NULL);
INSERT OR IGNORE INTO app_settings (key, value) VALUES ('ai_provider', 'OPENAI');
INSERT OR IGNORE INTO app_settings (key, value) VALUES ('ai_api_key_ref', NULL);
INSERT OR IGNORE INTO app_settings (key, value) VALUES ('notify_sound', 'true');
INSERT OR IGNORE INTO app_settings (key, value) VALUES ('first_launch', 'true');

CREATE TABLE IF NOT EXISTS categories (
    id         INTEGER  PRIMARY KEY AUTOINCREMENT,
    name       TEXT     NOT NULL UNIQUE COLLATE NOCASE,
    color_hex  TEXT     NOT NULL DEFAULT '#6C63FF',
    icon_code  TEXT,
    sort_order INTEGER  NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT (datetime('now'))
);

INSERT OR IGNORE INTO categories (name, color_hex, icon_code, sort_order) VALUES ('Personal', '#6C63FF', 'mdi2a-account', 0);
INSERT OR IGNORE INTO categories (name, color_hex, icon_code, sort_order) VALUES ('Work', '#FF6584', 'mdi2b-briefcase', 1);
INSERT OR IGNORE INTO categories (name, color_hex, icon_code, sort_order) VALUES ('Health', '#43D9AD', 'mdi2h-heart-pulse', 2);
INSERT OR IGNORE INTO categories (name, color_hex, icon_code, sort_order) VALUES ('Study', '#FFB347', 'mdi2b-book-open', 3);
INSERT OR IGNORE INTO categories (name, color_hex, icon_code, sort_order) VALUES ('Finance', '#64DFDF', 'mdi2c-cash', 4);

CREATE TABLE IF NOT EXISTS tasks (
    id              INTEGER  PRIMARY KEY AUTOINCREMENT,
    parent_task_id  INTEGER  DEFAULT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    title           TEXT     NOT NULL,
    description     TEXT     DEFAULT NULL,
    category_id     INTEGER  DEFAULT NULL REFERENCES categories(id) ON DELETE SET NULL,
    task_status     TEXT     NOT NULL DEFAULT 'PENDING' CHECK (task_status IN ('PENDING', 'DONE', 'DELAYED')),
    priority        TEXT     NOT NULL DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'CRITICAL')),
    due_date        DATE     DEFAULT NULL,
    due_time        TIME     DEFAULT NULL,
    notify_at       DATETIME DEFAULT NULL,
    section         TEXT     NOT NULL DEFAULT 'MY_DAY' CHECK (section IN ('MY_DAY', 'IMPORTANT', 'DO', 'DONT', 'HABITS', 'NOTIFY_LATER', 'COMPLETED', 'DELAYED', 'DELETED')),
    is_rolled_over  INTEGER  NOT NULL DEFAULT 0 CHECK (is_rolled_over IN (0, 1)),
    rollover_count  INTEGER  NOT NULL DEFAULT 0,
    sort_order      INTEGER  NOT NULL DEFAULT 0,
    is_deleted      INTEGER  NOT NULL DEFAULT 0 CHECK (is_deleted IN (0, 1)),
    deleted_at      DATETIME DEFAULT NULL,
    completed_at    DATETIME DEFAULT NULL,
    created_at      DATETIME NOT NULL DEFAULT (datetime('now')),
    updated_at      DATETIME NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_tasks_parent ON tasks(parent_task_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(task_status);
CREATE INDEX IF NOT EXISTS idx_tasks_section ON tasks(section);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON tasks(due_date);
CREATE INDEX IF NOT EXISTS idx_tasks_deleted ON tasks(is_deleted, section);

CREATE TRIGGER IF NOT EXISTS trg_tasks_updated_at AFTER UPDATE ON tasks FOR EACH ROW BEGIN UPDATE tasks SET updated_at = datetime('now') WHERE id = OLD.id; END;

CREATE TRIGGER IF NOT EXISTS trg_tasks_completed_at AFTER UPDATE OF task_status ON tasks FOR EACH ROW WHEN NEW.task_status = 'DONE' AND OLD.task_status != 'DONE' BEGIN UPDATE tasks SET completed_at = datetime('now') WHERE id = NEW.id; END;

CREATE TRIGGER IF NOT EXISTS trg_tasks_uncompleted AFTER UPDATE OF task_status ON tasks FOR EACH ROW WHEN NEW.task_status != 'DONE' AND OLD.task_status = 'DONE' BEGIN UPDATE tasks SET completed_at = NULL WHERE id = NEW.id; END;

CREATE TABLE IF NOT EXISTS habits (
    id              INTEGER  PRIMARY KEY AUTOINCREMENT,
    title           TEXT     NOT NULL,
    description     TEXT     DEFAULT NULL,
    category_id     INTEGER  DEFAULT NULL REFERENCES categories(id) ON DELETE SET NULL,
    recurrence_days TEXT     NOT NULL DEFAULT '1,2,3,4,5,6,7',
    color_hex       TEXT     NOT NULL DEFAULT '#6C63FF',
    icon_code       TEXT     DEFAULT 'mdi2c-checkbox-marked-circle',
    current_streak  INTEGER  NOT NULL DEFAULT 0,
    longest_streak  INTEGER  NOT NULL DEFAULT 0,
    habit_type      TEXT     NOT NULL DEFAULT 'BUILD' CHECK (habit_type IN ('BUILD', 'BREAK')),
    is_archived     INTEGER  NOT NULL DEFAULT 0 CHECK (is_archived IN (0, 1)),
    sort_order      INTEGER  NOT NULL DEFAULT 0,
    created_at      DATETIME NOT NULL DEFAULT (datetime('now')),
    updated_at      DATETIME NOT NULL DEFAULT (datetime('now'))
);

CREATE TRIGGER IF NOT EXISTS trg_habits_updated_at AFTER UPDATE ON habits FOR EACH ROW BEGIN UPDATE habits SET updated_at = datetime('now') WHERE id = OLD.id; END;

CREATE TABLE IF NOT EXISTS habit_logs (
    id        INTEGER  PRIMARY KEY AUTOINCREMENT,
    habit_id  INTEGER  NOT NULL REFERENCES habits(id) ON DELETE CASCADE,
    log_date  DATE     NOT NULL,
    status    TEXT     NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'DONE', 'SKIPPED', 'FAILED')),
    note      TEXT     DEFAULT NULL,
    logged_at DATETIME NOT NULL DEFAULT (datetime('now')),
    UNIQUE (habit_id, log_date)
);

CREATE INDEX IF NOT EXISTS idx_habit_logs_date ON habit_logs(log_date);
CREATE INDEX IF NOT EXISTS idx_habit_logs_habit_date ON habit_logs(habit_id, log_date);

CREATE TABLE IF NOT EXISTS daily_catchup_log (
    id           INTEGER  PRIMARY KEY AUTOINCREMENT,
    catchup_date DATE     NOT NULL,
    session_date DATE     NOT NULL,
    task_id      INTEGER  NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    action_taken TEXT     NOT NULL CHECK (action_taken IN ('MOVED_TO_TODAY', 'MARKED_DELAYED', 'MARKED_DONE', 'DISMISSED')),
    created_at   DATETIME NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_catchup_session ON daily_catchup_log(session_date);

CREATE TABLE IF NOT EXISTS notifications (
    id           INTEGER  PRIMARY KEY AUTOINCREMENT,
    task_id      INTEGER  DEFAULT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    title        TEXT     NOT NULL,
    body         TEXT     DEFAULT NULL,
    notify_at    DATETIME NOT NULL,
    is_sent      INTEGER  NOT NULL DEFAULT 0 CHECK (is_sent IN (0, 1)),
    sent_at      DATETIME DEFAULT NULL,
    is_dismissed INTEGER  NOT NULL DEFAULT 0 CHECK (is_dismissed IN (0, 1)),
    created_at   DATETIME NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS ai_summaries (
    id            INTEGER  PRIMARY KEY AUTOINCREMENT,
    summary_date  DATE     NOT NULL UNIQUE,
    provider      TEXT     NOT NULL,
    model_used    TEXT     DEFAULT NULL,
    prompt_tokens INTEGER  DEFAULT NULL,
    prompt_text   TEXT     DEFAULT NULL,
    response_text TEXT     NOT NULL,
    generated_at  DATETIME NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS dodoants (
    id          INTEGER  PRIMARY KEY AUTOINCREMENT,
    title       TEXT     NOT NULL,
    description TEXT     DEFAULT NULL,
    type        TEXT     NOT NULL DEFAULT 'DO' CHECK (type IN ('DO', 'DONT')),
    color_hex   TEXT     NOT NULL DEFAULT '#10B981',
    sort_order  INTEGER  NOT NULL DEFAULT 0,
    is_archived INTEGER  NOT NULL DEFAULT 0 CHECK (is_archived IN (0, 1)),
    created_at  DATETIME NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS dodont_logs (
    id          INTEGER  PRIMARY KEY AUTOINCREMENT,
    dodont_id   INTEGER  NOT NULL REFERENCES dodoants(id) ON DELETE CASCADE,
    log_date    DATE     NOT NULL,
    status      TEXT     NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    logged_at   DATETIME NOT NULL DEFAULT (datetime('now')),
    UNIQUE (dodont_id, log_date)
);

CREATE INDEX IF NOT EXISTS idx_dodont_logs_date ON dodont_logs(log_date);

CREATE TABLE IF NOT EXISTS users ( id INTEGER PRIMARY KEY AUTOINCREMENT, full_name TEXT NOT NULL, username TEXT NOT NULL UNIQUE COLLATE NOCASE, avatar_color TEXT NOT NULL DEFAULT '#6C63FF', accent_color TEXT NOT NULL DEFAULT '#6C63FF', pin_hash TEXT DEFAULT NULL, is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1)), sort_order INTEGER NOT NULL DEFAULT 0, created_at DATETIME NOT NULL DEFAULT (datetime('now')) );

CREATE TABLE IF NOT EXISTS shared_tasks ( id INTEGER PRIMARY KEY AUTOINCREMENT, origin_task_id INTEGER NOT NULL, copy_task_id INTEGER NOT NULL REFERENCES tasks(id) ON DELETE CASCADE, shared_by_user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, shared_with_user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, share_group TEXT NOT NULL, created_at DATETIME NOT NULL DEFAULT (datetime('now')) );

CREATE INDEX IF NOT EXISTS idx_shared_tasks_group ON shared_tasks(share_group);
CREATE INDEX IF NOT EXISTS idx_shared_tasks_copy ON shared_tasks(copy_task_id);

CREATE INDEX IF NOT EXISTS idx_tasks_user ON tasks(user_id);
CREATE INDEX IF NOT EXISTS idx_habits_user ON habits(user_id);
CREATE INDEX IF NOT EXISTS idx_dodoants_user ON dodoants(user_id);
CREATE INDEX IF NOT EXISTS idx_tasks_user_section ON tasks(user_id, section, is_deleted);
CREATE INDEX IF NOT EXISTS idx_tasks_user_status ON tasks(user_id, task_status, is_deleted);