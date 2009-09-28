DROP TABLE IF EXISTS `wave`;
CREATE TABLE `wave` (
	`id` INT(10) UNSIGNED PRIMARY KEY NOT NULL AUTO_INCREMENT,
	`page_id` INT(10) UNSIGNED NOT NULL,
	`wave_id` VARCHAR(100),

	INDEX page_idx (page_id, wave_id),
	INDEX wave_idx (wave_id),
	FOREIGN KEY (`page_id`) REFERENCES `page`(`page_id`)
		ON UPDATE CASCADE ON DELETE CASCADE
) 

