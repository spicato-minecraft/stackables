package stackables.gametest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public class ServerMixinApplySweepGameTest {

	private static final String MIXIN_CONFIG = "stackables.mixins.json";
	private static final String MIXIN_DESCRIPTOR = "Lorg/spongepowered/asm/mixin/Mixin;";

	@GameTest(maxTicks = 100)
	public void serverMixinApplySweep(GameTestHelper context) {
		List<String> mixinClasses = serverMixinClasses();
		context.assertFalse(mixinClasses.isEmpty(), "stackables server mixins should be listed");

		int loadedTargets = 0;
		for (String mixinClass : mixinClasses) {
			ClassNode node = readMixinClass(mixinClass);
			context.assertFalse(isAccessor(node), mixinClass + " is an accessor");
			List<String> binaryNames = mixinTargetBinaryNames(node);
			context.assertFalse(binaryNames.isEmpty(), mixinClass + " must declare @Mixin targets");
			for (String binaryName : binaryNames) {
				loadTarget(binaryName);
				loadedTargets++;
			}
		}
		context.assertValueEqual(mixinClasses.size(), loadedTargets, "each server mixin declares one target");
		context.succeed();
	}

	private static List<String> serverMixinClasses() {
		String json = readResource(MIXIN_CONFIG);
		JsonObject root = JsonParser.parseString(json).getAsJsonObject();
		String packageName = root.get("package").getAsString();
		List<String> classes = new ArrayList<>();
		for (JsonElement element : root.getAsJsonArray("mixins")) {
			classes.add(packageName + "." + element.getAsString());
		}
		return classes;
	}

	private static ClassNode readMixinClass(String binaryName) {
		String resource = binaryName.replace('.', '/') + ".class";
		try (InputStream in = ServerMixinApplySweepGameTest.class.getClassLoader().getResourceAsStream(resource)) {
			if (in == null) {
				throw new IllegalStateException("missing mixin class " + binaryName);
			}
			ClassNode node = new ClassNode();
			new ClassReader(in).accept(node, ClassReader.SKIP_CODE);
			return node;
		} catch (IOException e) {
			throw new IllegalStateException("failed to read mixin class " + binaryName, e);
		}
	}

	private static String readResource(String resource) {
		try (InputStream in = ServerMixinApplySweepGameTest.class.getClassLoader().getResourceAsStream(resource)) {
			if (in == null) {
				throw new IllegalStateException("missing resource " + resource);
			}
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("failed to read " + resource, e);
		}
	}

	private static boolean isAccessor(ClassNode node) {
		for (AnnotationNode annotation : annotations(node)) {
			if ("Lorg/spongepowered/asm/mixin/gen/Accessor;".equals(annotation.desc)
				|| "Lorg/spongepowered/asm/mixin/gen/Invoker;".equals(annotation.desc)) {
				return true;
			}
		}
		return false;
	}

	private static List<AnnotationNode> annotations(ClassNode node) {
		List<AnnotationNode> annotations = new ArrayList<>();
		if (node.visibleAnnotations != null) {
			annotations.addAll(node.visibleAnnotations);
		}
		if (node.invisibleAnnotations != null) {
			annotations.addAll(node.invisibleAnnotations);
		}
		return annotations;
	}

	private static List<String> mixinTargetBinaryNames(ClassNode node) {
		List<String> names = new ArrayList<>();
		for (AnnotationNode annotation : annotations(node)) {
			if (!MIXIN_DESCRIPTOR.equals(annotation.desc) || annotation.values == null) {
				continue;
			}
			for (int i = 0; i < annotation.values.size(); i += 2) {
				String key = (String) annotation.values.get(i);
				Object value = annotation.values.get(i + 1);
				if ("value".equals(key)) {
					addClassNames(names, value);
				} else if ("targets".equals(key)) {
					addTargetNames(names, value);
				}
			}
		}
		return names;
	}

	private static void addClassNames(List<String> names, Object value) {
		if (value instanceof Type asmType) {
			names.add(asmType.getClassName());
			return;
		}
		if (value instanceof List<?> types) {
			for (Object type : types) {
				if (type instanceof Type asmType) {
					names.add(asmType.getClassName());
				}
			}
		}
	}

	private static void addTargetNames(List<String> names, Object value) {
		if (value instanceof String target) {
			names.add(target.replace('/', '.'));
			return;
		}
		if (value instanceof List<?> targets) {
			for (Object target : targets) {
				names.add(((String) target).replace('/', '.'));
			}
		}
	}

	private static void loadTarget(String binaryName) {
		try {
			Class.forName(binaryName);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("missing mixin target " + binaryName, e);
		}
	}
}
