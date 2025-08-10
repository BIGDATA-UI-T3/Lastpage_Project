-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema funeralDB
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema funeralDB
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `funeralDB` DEFAULT CHARACTER SET utf8 ;
USE `funeralDB` ;

-- -----------------------------------------------------
-- Table `funeralDB`.`TBL_user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `funeralDB`.`TBL_user` (
  `user_id` INT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `age` INT NOT NULL,
  `address` VARCHAR(45) NOT NULL,
  `gender` VARCHAR(45) NOT NULL,
  `phone` INT NOT NULL,
  PRIMARY KEY (`user_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `funeralDB`.`TBL_funeral`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `funeralDB`.`TBL_funeral` (
  `funeral_id` INT NOT NULL,
  `pet_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  PRIMARY KEY (`funeral_id`, `pet_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `funeralDB`.`TBL_pet`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `funeralDB`.`TBL_pet` (
  `pet_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `age` VARCHAR(45) NOT NULL,
  `gender` VARCHAR(45) NOT NULL,
  `species` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`pet_id`),
  INDEX `fk_TBL_pet_TBL_user1_idx` (`user_id` ASC) VISIBLE,
  CONSTRAINT `fk_TBL_pet_TBL_user1`
    FOREIGN KEY (`user_id`)
    REFERENCES `funeralDB`.`TBL_user` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `funeralDB`.`TBL_goods`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `funeralDB`.`TBL_goods` (
  `goods_id` INT NOT NULL,
  `pet_id` INT NOT NULL,
  `funeral_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `stone-bone` VARCHAR(45) NOT NULL,
  `acc-fur` VARCHAR(45) NULL,
  `urn` VARCHAR(45) NOT NULL,
  `plush toy` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`goods_id`),
  INDEX `fk_TBL_goods_TBL_pet1_idx` (`pet_id` ASC, `funeral_id` ASC) VISIBLE,
  CONSTRAINT `fk_TBL_goods_TBL_pet1`
    FOREIGN KEY (`pet_id`)
    REFERENCES `funeralDB`.`TBL_pet` (`pet_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `funeralDB`.`TBL_payment`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `funeralDB`.`TBL_payment` (
  `payment_id` INT NOT NULL,
  `reserve_id` INT NOT NULL,
  `method` VARCHAR(45) NOT NULL,
  `price` INT NOT NULL,
  `payment_date` DATETIME NOT NULL,
  `payment_status` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`payment_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `funeralDB`.`TBL_counselor`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `funeralDB`.`TBL_counselor` (
  `counselor_id` INT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `age` INT NOT NULL,
  `career` VARCHAR(45) NOT NULL,
  `email` VARCHAR(45) NOT NULL,
  `phone` INT NOT NULL,
  PRIMARY KEY (`counselor_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `funeralDB`.`TBL_counsel`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `funeralDB`.`TBL_counsel` (
  `counsel_id` INT NOT NULL,
  `counselor_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  PRIMARY KEY (`counsel_id`),
  INDEX `fk_TBL_counsel_TBL_counselor1_idx` (`counselor_id` ASC) VISIBLE,
  CONSTRAINT `fk_TBL_counsel_TBL_counselor1`
    FOREIGN KEY (`counselor_id`)
    REFERENCES `funeralDB`.`TBL_counselor` (`counselor_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `funeralDB`.`TBL_reserve`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `funeralDB`.`TBL_reserve` (
  `reserve_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `funeral_id` INT NOT NULL,
  `goods_id` INT NOT NULL,
  `payment_id` INT NOT NULL,
  `counsel_id` INT NOT NULL,
  PRIMARY KEY (`reserve_id`),
  INDEX `fk_TBL_rerserve_TBL_user_idx` (`user_id` ASC) VISIBLE,
  INDEX `fk_TBL_rerserve_TBL_funeral1_idx` (`funeral_id` ASC) VISIBLE,
  INDEX `fk_TBL_rerserve_TBL_goods1_idx` (`goods_id` ASC) VISIBLE,
  INDEX `fk_TBL_rerserve_TBL_purchase1_idx` (`payment_id` ASC) VISIBLE,
  INDEX `fk_TBL_reserve_TBL_counsel1_idx` (`counsel_id` ASC) VISIBLE,
  CONSTRAINT `fk_TBL_rerserve_TBL_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `funeralDB`.`TBL_user` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_TBL_rerserve_TBL_funeral1`
    FOREIGN KEY (`funeral_id`)
    REFERENCES `funeralDB`.`TBL_funeral` (`funeral_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_TBL_rerserve_TBL_goods1`
    FOREIGN KEY (`goods_id`)
    REFERENCES `funeralDB`.`TBL_goods` (`goods_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_TBL_rerserve_TBL_purchase1`
    FOREIGN KEY (`payment_id`)
    REFERENCES `funeralDB`.`TBL_payment` (`payment_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_TBL_reserve_TBL_counsel1`
    FOREIGN KEY (`counsel_id`)
    REFERENCES `funeralDB`.`TBL_counsel` (`counsel_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `funeralDB`.`TBL_commu`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `funeralDB`.`TBL_commu` (
  `commu_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `contents` VARCHAR(45) NOT NULL,
  `updated_at` VARCHAR(45) NOT NULL,
  `created_at` VARCHAR(45) NOT NULL,
  `is_deleted` VARCHAR(45) NOT NULL,
  `comments` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`commu_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `funeralDB`.`TBL_active`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `funeralDB`.`TBL_active` (
  `active_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `commu_id` INT NOT NULL,
  PRIMARY KEY (`active_id`),
  INDEX `fk_TBL_active_TBL_user1_idx` (`user_id` ASC) VISIBLE,
  INDEX `fk_TBL_active_TBL_commu1_idx` (`commu_id` ASC) VISIBLE,
  CONSTRAINT `fk_TBL_active_TBL_user1`
    FOREIGN KEY (`user_id`)
    REFERENCES `funeralDB`.`TBL_user` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_TBL_active_TBL_commu1`
    FOREIGN KEY (`commu_id`)
    REFERENCES `funeralDB`.`TBL_commu` (`commu_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `funeralDB`.`TBL_my_page`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `funeralDB`.`TBL_my_page` (
  `my_page_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `reserve_id` INT NOT NULL,
  PRIMARY KEY (`my_page_id`, `user_id`),
  INDEX `fk_TBL_my_page_TBL_user1_idx` (`user_id` ASC) VISIBLE,
  CONSTRAINT `fk_TBL_my_page_TBL_user1`
    FOREIGN KEY (`user_id`)
    REFERENCES `funeralDB`.`TBL_user` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `funeralDB`.`TBL_CS`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `funeralDB`.`TBL_CS` (
  `CS_id` INT NOT NULL,
  `phone_admin` VARCHAR(45) NULL,
  `email_admin` VARCHAR(45) NULL,
  `git_admin` VARCHAR(45) NULL,
  `FAQ` VARCHAR(45) NULL,
  PRIMARY KEY (`CS_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `funeralDB`.`TBL_QnA`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `funeralDB`.`TBL_QnA` (
  `QnA_id` INT NOT NULL,
  `FAQ` VARCHAR(45) NOT NULL,
  `question-updated_at` VARCHAR(45) NOT NULL,
  `question-created_at` VARCHAR(45) NOT NULL,
  `question_is_deleted` VARCHAR(45) NOT NULL,
  `answer-created_at` VARCHAR(45) NOT NULL,
  `answer_is_deleted` VARCHAR(45) NOT NULL,
  `answer-updated_at` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`QnA_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `funeralDB`.`TBL_ask`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `funeralDB`.`TBL_ask` (
  `ask_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `QnA_id` INT NOT NULL,
  INDEX `fk_TBL_ask_TBL_user1_idx` (`user_id` ASC) VISIBLE,
  INDEX `fk_TBL_ask_TBL_QnA1_idx` (`QnA_id` ASC) VISIBLE,
  PRIMARY KEY (`ask_id`),
  CONSTRAINT `fk_TBL_ask_TBL_user1`
    FOREIGN KEY (`user_id`)
    REFERENCES `funeralDB`.`TBL_user` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_TBL_ask_TBL_QnA1`
    FOREIGN KEY (`QnA_id`)
    REFERENCES `funeralDB`.`TBL_QnA` (`QnA_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
