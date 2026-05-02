<?php
/**
 * Created by PhpStorm.
 * User: jkorn2324
 * Date: 2020-06-11
 * Time: 23:11
 */

declare(strict_types=1);

namespace jkorn\pvpcore\player;

use jkorn\pvpcore\PvPCore;
use jkorn\pvpcore\utils\PvPCKnockback;
use jkorn\pvpcore\utils\Utils;
use pocketmine\event\entity\EntityDamageByEntityEvent;
use pocketmine\event\entity\EntityDamageEvent;
use pocketmine\form\Form;
use pocketmine\math\Vector3;
use pocketmine\player\Player;
use pocketmine\utils\TextFormat;
use stdClass;

class PvPCPlayer extends Player
{

    /** @var stdClass|null - The pvp area info. */
    private $pvpAreaInfo = null;
    /** @var bool - Determines if player is looking at a form. */
    private $lookingAtForm = false;
    /** @var PvPCKnockback|null */
    private $activeKnockback = null;

    /**
     * Sets the first position of the area information.
     */
    public function setFirstPos(): void
    {
        if ($this->pvpAreaInfo === null) {
            $this->pvpAreaInfo = new stdClass();
        }

        $this->pvpAreaInfo->firstPos = $this->asVector3();

        $this->sendMessage(Utils::getPrefix() . TextFormat::GREEN . " Successfully set the first position of the PvPArea.");
    }

    /**
     * Sets the second position of the area information.
     */
    public function setSecondPos(): void
    {
        if ($this->pvpAreaInfo === null) {
            $this->pvpAreaInfo = new stdClass();
        }

        $this->pvpAreaInfo->secondPos = $this->asVector3();

        $this->sendMessage(Utils::getPrefix() . TextFormat::GREEN . " Successfully set the second position of the PvPArea.");
    }


    /**
     * @return stdClass|null
     *
     * Gets the area information of the player.
     */
    public function getAreaInfo(): ?stdClass
    {
        return $this->pvpAreaInfo;
    }

    /**
     * @param string $name
     *
     * Creates the area based on the name.
     */
    public function createArea(string $name): void
    {
        if (PvPCore::getAreaHandler()->createArea($this->pvpAreaInfo, $name, $this)) {
            $this->sendMessage(Utils::getPrefix() . TextFormat::GREEN . " Successfully created a new PvPArea.");
            $this->pvpAreaInfo = null;
        }
    }

    /**
     * @param float $x
     * @param float $z
     * @param float $force
     * @param float|null $verticalLimit
     *
     * Gives the player knockback values.
     */
    public function knockBack(float $x, float $z, float $force = self::DEFAULT_KNOCKBACK_FORCE, ?float $verticalLimit = self::DEFAULT_KNOCKBACK_VERTICAL_LIMIT): void
    {
        $xzKB = $force;
        $yKb = $verticalLimit ?? $force;
        if ($this->activeKnockback instanceof PvPCKnockback) {
            $xzKB = $this->activeKnockback->getXZKb();
            $yKb = $this->activeKnockback->getYKb();
        }

        $f = sqrt($x * $x + $z * $z);
        if ($f <= 0) {
            return;
        }
        if (mt_rand() / mt_getrandmax() > $this->knockbackResistanceAttr->getValue()) {
            $f = 1 / $f;

            $motionX = $this->motion->x / 2;
            $motionY = $this->motion->y / 2;
            $motionZ = $this->motion->z / 2;
            $motionX += $x * $f * $xzKB;
            $motionY += $yKb;
            $motionZ += $z * $f * $xzKB;

            if ($motionY > $yKb) {
                $motionY = $yKb;
            }

            $this->setMotion(new Vector3($motionX, $motionY, $motionZ));
        }
    }

    /**
     * @param EntityDamageEvent $source
     *
     * Called when the player gets attacked, overriden to change the attack speed.
     */
    public function attack(EntityDamageEvent $source): void
    {
        if ($source instanceof EntityDamageByEntityEvent) {
            $damager = $source->getDamager();
            if ($damager instanceof Player) {
                $knockback = Utils::getKnockbackFor($this, $damager);
                if($knockback instanceof PvPCKnockback) {
                    $source->setAttackCooldown(max(0, $knockback->getSpeed()));
                    $this->activeKnockback = $knockback;
                }
            }
        }

        try {
            parent::attack($source);
        } finally {
            $this->activeKnockback = null;
        }
    }

    /**
     * @param int $formId
     * @param mixed $responseData
     * @return bool
     *
     * Handled when the form was submitted.
     */
    public function onFormSubmit(int $formId, mixed $responseData): bool
    {
        $this->lookingAtForm = false;
        return parent::onFormSubmit($formId, $responseData);
    }

    /**
     * @param Form $form
     *
     * Sends the form to the player to be processed.
     */
    public function sendForm(Form $form): void
    {
        if(!$this->lookingAtForm)
        {
            $this->lookingAtForm = true;
            parent::sendForm($form);
        }
    }
}
