-- 校园活动管理系统 V1.0 建库建表脚本
CREATE DATABASE IF NOT EXISTS campus_activity DEFAULT CHARACTER SET utf8mb4;
USE campus_activity;

CREATE TABLE IF NOT EXISTS `user` (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  username      VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
  password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密后的密码',
  role          VARCHAR(10)  NOT NULL COMMENT '角色：STUDENT/TEACHER',
  created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB COMMENT '用户';

CREATE TABLE IF NOT EXISTS activity (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id       BIGINT       NOT NULL COMMENT '发布教师 id',
  title            VARCHAR(100) NOT NULL COMMENT '活动标题',
  description      TEXT         COMMENT '活动描述',
  location         VARCHAR(100) COMMENT '活动地点',
  start_time       DATETIME     NOT NULL COMMENT '开始时间',
  end_time         DATETIME     NOT NULL COMMENT '结束时间',
  signup_deadline  DATETIME     NOT NULL COMMENT '报名截止时间',
  max_participants INT          NOT NULL COMMENT '人数上限',
  status           VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/CANCELLED',
  created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
  KEY idx_teacher (teacher_id)
) ENGINE = InnoDB COMMENT '活动';

CREATE TABLE IF NOT EXISTS registration (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  activity_id   BIGINT   NOT NULL COMMENT '活动 id',
  student_id    BIGINT   NOT NULL COMMENT '学生 id',
  registered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_activity_student (activity_id, student_id)
) ENGINE = InnoDB COMMENT '报名记录';
