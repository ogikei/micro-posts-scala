CREATE TABLE `favorites` (
  `id`            BIGINT        AUTO_INCREMENT,
  `user_id`       BIGINT        NOT NULL,
  `micro_post_id` BIGINT        NOT NULL,
  `create_at`     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_at`     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(`id`),
  FOREIGN KEY (`user_id`) REFERENCES micro_posts(`user_id`),
  FOREIGN KEY (`micro_post_id`) REFERENCES micro_posts(`id`),
  UNIQUE(`user_id`, `micro_post_id`)
) ENGINE=InnoDB;
