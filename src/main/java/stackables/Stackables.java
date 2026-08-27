package stackables;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Stackables implements ModInitializer {
	public static final String MOD_ID = "stackables";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		StackableItems.register();
		LOGGER.info("Stackables: potions and enchanted books stack to {}", StackableItems.MAX_STACK_SIZE);
	}
}
