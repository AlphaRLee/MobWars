package io.github.alpharlee.mobwars;

import com.rlee.mobwars.mobs.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_10_R1.BiomeBase;
import net.minecraft.server.v1_10_R1.EntityBlaze;
import net.minecraft.server.v1_10_R1.EntityCreeper;
import net.minecraft.server.v1_10_R1.EntityEnderman;
import net.minecraft.server.v1_10_R1.EntityEndermite;
import net.minecraft.server.v1_10_R1.EntityGhast;
import net.minecraft.server.v1_10_R1.EntityGiantZombie;
import net.minecraft.server.v1_10_R1.EntityGuardian;
import net.minecraft.server.v1_10_R1.BiomeBase.BiomeMeta; //Test, update in Spigot 1.9 no longer allows import net.minecraft.server.v1_9_R1.BiomeMeta;
import net.minecraft.server.v1_10_R1.EntityCaveSpider;
import net.minecraft.server.v1_10_R1.EntityChicken;
import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.EntityIronGolem;
import net.minecraft.server.v1_10_R1.EntityLiving;
import net.minecraft.server.v1_10_R1.EntityMagmaCube;
import net.minecraft.server.v1_10_R1.EntityPigZombie;
import net.minecraft.server.v1_10_R1.EntitySheep;
import net.minecraft.server.v1_10_R1.EntityShulker;
import net.minecraft.server.v1_10_R1.EntitySilverfish;
import net.minecraft.server.v1_10_R1.EntitySkeleton;
import net.minecraft.server.v1_10_R1.EntitySlime;
import net.minecraft.server.v1_10_R1.EntitySpider;
import net.minecraft.server.v1_10_R1.EntityTypes;
import net.minecraft.server.v1_10_R1.EntityVillager;
import net.minecraft.server.v1_10_R1.EntityWitch;
import net.minecraft.server.v1_10_R1.EntityZombie;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
 
//Copy and modification of code found from: https://bukkit.org/threads/how-to-override-default-minecraft-mobs.216788/
//Please document what can be found

/**
 * Allows registration for custom entities
 */
public enum MWEntityType 
{
	/*
	 * Parameters:
	 * ENUM_NAME: Name of enumerator. Can be anything
	 * String name: Name of entity being overriden
	 * int id: ID of the entity
	 * org.bukkit.entity.Entity entityType: Bukkit convenience class
	 * Class<? extends EntityInsentient> nmsClass: Class being overriden
	 * Class<? extends EntityInsentient> customClass: Customized class overriding
	 */
	CREEPER("Creeper", 50, EntityType.CREEPER, EntityCreeper.class, MWEntityCreeper.class),
	SKELETON("Skeleton", 51, EntityType.SKELETON, EntitySkeleton.class, MWEntitySkeleton.class),
    SPIDER("Spider", 52, EntityType.SPIDER, EntitySpider.class, MWEntitySpider.class),
    GIANT("Giant", 53, EntityType.GIANT, EntityGiantZombie.class, MWEntityGiantZombie.class),
	ZOMBIE("Zombie", 54, EntityType.ZOMBIE, EntityZombie.class, MWEntityZombie.class), 
	//SLIME("Slime", 55, EntityType.SLIME, EntitySlime.class, MWEntitySlime.class),
    GHAST("Ghast", 56, EntityType.GHAST, EntityGhast.class, MWEntityGhast.class),
    //PIG_ZOMBIE("PigZombie", 57, EntityType.PIG_ZOMBIE, EntityPigZombie.class, MWEntityPigZombie.class),
    ENDERMAN("Enderman", 58, EntityType.ENDERMAN, EntityEnderman.class, MWEntityEnderman.class),
    //CAVE_SPIDER("CaveSpider", 59, EntityType.CAVE_SPIDER, EntityCaveSpider.class, MWEntityCaveSpider.class),
    //SILVERFISH("Silverfish", 60, EntityType.SILVERFISH, EntitySilverfish.class, MWEntitySilverfish.class),
    BLAZE("Blaze", 61, EntityType.BLAZE, EntityBlaze.class, MWEntityBlaze.class),
    //MAGMA_CUBE("LavaSlime", 62, EntityType.MAGMA_CUBE, EntityMagmaCube.class, MWEntityMagmaCube.class),
    
    WITCH("Witch", 66, EntityType.WITCH, EntityWitch.class, MWEntityWitch.class),
    //ENDERMITE("Endermite", 67, EntityType.ENDERMITE, EntityEndermite.class, MWEntityEndermite.class),
    //GUARDIAN("Guardian", 68, EntityType.GUARDIAN, EntityGuardian.class, MWEntityGuardian.class),
    //SHULKER("Shulker", 69, EntityType.SHULKER, EntityShulker.class, MWEntityShulker.class),
    
    SHEEP("Sheep", 91, EntityType.SHEEP, EntitySheep.class, MWEntitySheep.class),
    
    CHICKEN("Chicken", 93, EntityType.CHICKEN, EntityChicken.class, MWEntityChicken.class),
    
    //IRON_GOLEM("VillagerGolem", 99, EntityType.IRON_GOLEM, EntityIronGolem.class, MWEntityIronGolem.class),
    
    VILLAGER("Villager", 120, EntityType.VILLAGER, EntityVillager.class, MWEntityVillager.class);
	
	private String name;
	private int id;
	private EntityType entityType;
	private Class<? extends EntityInsentient> nmsClass;
	private Class<? extends EntityInsentient> customClass;
	//Used to lookup the enum from the Bukkit Entity Type. Initialized in this.registerEntities()
	private static Map<EntityType, MWEntityType> lookupByEntityType;
	
	private MWEntityType(String name, int id, EntityType entityType, Class<? extends EntityInsentient> nmsClass, Class<? extends EntityInsentient> customClass) 
	{
		this.name = name;
		this.id = id;
		this.entityType = entityType;
		this.nmsClass = nmsClass;
		this.customClass = customClass;
	}
	
	public String getName() 
	{
		return this.name;
	}
 
	public int getID() 
	{
		return this.id;
	}
 
	public EntityType getEntityType() 
	{
		return this.entityType;
	}
 
	public Class<? extends EntityInsentient> getNMSClass() 
	{
		return this.nmsClass;
	}
 
	public Class<? extends EntityInsentient> getCustomClass() 
	{
		return this.customClass;
	}
	
	/**
	 * Return the MWEntityType associated to the Bukkit EntityType
	 * @param type Bukkit EntityType to lookup
	 * @return MWEntityType associated with it
	 */
	public static MWEntityType getMWTypeByEntityType(EntityType type)
	{
		return lookupByEntityType.get(type);
	}
	
	/**
	 * Initialize lookup table for this.lookupByEntityType
	 */
	public static void initializeLookup()
	{
		lookupByEntityType = new HashMap<EntityType, MWEntityType>();
		
		for(MWEntityType type : MWEntityType.values())
		{
			lookupByEntityType.put(type.getEntityType(), type);
		}
	}
	
	/**
	 * Convert a Bukkit-type LivingEntity to an NMS-type EntityLiving
	 * @param entity Bukkit entity to convert
	 * @return NMS EntityLiving equivalent
	 */
	public static EntityLiving getNMSLivingFromBukkitLiving(LivingEntity entity)
	{
		return ((CraftLivingEntity) entity).getHandle();
	}
	
	/**
	 * Convert a Bukkit-type LivingEntity to an NMS-type EntityInsentient if applicable. Returns null if entity cannot be casted to insentient
	 * @param entity Bukkit LivingEntity to convert
	 * @return NMS EntityInsentient equivalent if applicable, null otherwise
	 */
	public static EntityInsentient getNMSInsentientFromBukkitLiving(LivingEntity entity)
	{
		EntityLiving nmsEntityLiving = getNMSLivingFromBukkitLiving(entity);
		
		//Run a quick check that the entity can be cast
		return nmsEntityLiving instanceof EntityInsentient ? (EntityInsentient) nmsEntityLiving : null;
	}
	
	/**
	 * Convert an NMS-type EntityLiving to a Bukkit-type LivingEntity
	 * @param entity NMS entity to convert
	 * @return Bukkit LivingEntity equivalent
	 */
	public static LivingEntity getBukkitLivingFromNMSLiving(EntityLiving entity)
	{
		return (LivingEntity) entity.getBukkitEntity();
	}
	
	/**
	 * Get the display name of this entity (eg. RED Zombie). The team will show up in color and the entity type in white
	 * @param entity Entity to display name of
	 * @param addTrailingSpace Set to true to add a " " to the end of the name
	 * @param boldedTeam Set to true to bold the team
	 * @return Display name of entity.
	 */
	public static String getEntityName(EntityInsentient entity, boolean addTrailingSpace, boolean boldedTeam)
	{
		String name = "";
		
		MWEntity mwEntity;
		Team team;
		LivingEntity bukkitEntity = MWEntityType.getBukkitLivingFromNMSLiving(entity);
		
		if (entity instanceof MWEntity)
		{
			mwEntity = (MWEntity) entity;
			team = mwEntity.getTeam();
			
			if (team != null && team != Team.NO_TEAM)
			{
				name = team.getChatColor() + "" + (boldedTeam ? ChatColor.BOLD : "") + team.getName() + ChatColor.RESET + " ";
			}
		}
		
		if (bukkitEntity != null && bukkitEntity.getType() != null)
		{
			name += ChatColor.WHITE + bukkitEntity.getType().getName();
		}
		else
		{
			name += ChatColor.WHITE + "Entity";
		}
		
		name += ChatColor.RESET;
		
		if (addTrailingSpace)
		{
			name += " ";
		}
		
		return name;
	}
	
	/**
	 * Get the location of an NMS EntityLiving
	 * @param entity Entity to get location of
	 * @return Location of entity
	 */
	public static Location getEntityLocation(EntityLiving entity)
	{
		return MWEntityType.getBukkitLivingFromNMSLiving(entity).getLocation();
	}
	
	/**
	 * Convert an arrayList of EntityInsentients to an arraylist of EntityLiving
	 * @param insentientList ArrayList of NMS EntityInsentients to convert
	 * @return ArrayList of NMS EntityLiving versions of the inputted list
	 */
	public static ArrayList<EntityLiving> getEntityLivingListFromInsentients(ArrayList<EntityInsentient> insentientList)
	{
		return new ArrayList<EntityLiving>(insentientList);
	}
	
	/**
	 * Register the MW entities.
	 */
	public static void registerEntities() 
	{
		//Lookup initializer
		initializeLookup();
		
		//Sample code modified from https://www.spigotmc.org/threads/solved-nms-entity-invinceble-but-can-hear-sounds.74371/
		for (MWEntityType entity: values())
		{
			//Write entity to map
			a(entity.getCustomClass(), entity.getName(), entity.getID());
			//Use below method to register entity into game
			entity.registerEntity(entity.getName(), entity.getID(), entity.getNMSClass(), entity.getCustomClass());
		}
		
		//Archive section of code from: https://bukkit.org/threads/how-to-override-default-minecraft-mobs.216788/
		//Does not look compatible with 1.9, unsure on compatibility with 1.8 or 1.10
		//CODE NOW REMOVED, PLEASE VISIT LINK ABOVE
    }
	
	//Modified sample from: https://www.spigotmc.org/threads/solved-nms-entity-invinceble-but-can-hear-sounds.74371/page-2
	public void registerEntity(String name, int id, Class<? extends EntityInsentient> nmsClass, Class<? extends EntityInsentient> customClass){
        try {
           
//            List<Map<?, ?>> dataMap = new ArrayList<Map<?, ?>>();
//            for (Field f : EntityTypes.class.getDeclaredFields()){
//            	//If the field (either method, constructor, or variable) is a map
//                if (f.getType().getSimpleName().equals(Map.class.getSimpleName())){
//                	
//                	//Set it to accessible
//                    f.setAccessible(true);
//                    
//                    //Add the map to the dataMap var
//                    dataMap.add((Map<?, ?>) f.get(null));
//                }
//            }
//           
//            //Minecraft will prevent entities with identical names or identical IDs from spawning
//            //Override this by accessing the "name" map and the "id" map and removing them
//            //If the second map within dataMap contains the key "id"
//            if (dataMap.get(2).containsKey(id)) {
//            	//Remove the map elements from maps 0 and 2 (assumed to be checked for spawn requirements)
//            	//WARNING: This method is highly dependent that only the names may change in the future, not the quantity of lists or their position
//                dataMap.get(0).remove(name);
//                dataMap.get(2).remove(id);
//            }
           
            //Now with the original entity removed, add it back in via the "a" method
            //This method will register the new entity
            //Get the method from EntityTypes.class labelled "a"
            Method method = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
            method.setAccessible(true);
            //Force method "a" to run
            method.invoke(null, customClass, name, id);
           
            //BiomeMeta registration snippet copied from:
            //https://bukkit.org/threads/nms-custom-entity-is-like-invisible.322708/
            //WARNING: This source was supplied from a 1.7.10 version of Minecraft. Assuming reflection will bypass this issue
            //Set the "default" class for every entity to be that of the MW entities
            //Use reflection for accessing even after name changes
            //////KLUDGE: Attempting to remove this sample to attempt to only affect manually spawned entities
            
//            for (Field f : BiomeBase.class.getDeclaredFields()) {
//                if (f.getType().getSimpleName().equals(BiomeBase.class.getSimpleName())) {
//                    if (f.get(null) != null) {
// 
//                       /*
//                        * This piece of code is being called for every biome,
//                        * we are going to loop through all fields in the
//                        * BiomeBase class so we can detect which of them are
//                        * Lists (again, to prevent problems when the field's
//                        * name changes), by doing this we can easily get the 4
//                        * required lists without using the name (which probably
//                        * changes every version)
//                        */
//                        for (Field list : BiomeBase.class.getDeclaredFields()) {
//                            if (list.getType().getSimpleName().equals(List.class.getSimpleName())) {
//                                list.setAccessible(true);
//                                @SuppressWarnings("unchecked")
//                                //Get the contents of the list and store them as a list of BiomeMeta
//                                List<BiomeMeta> metaList = (List<BiomeMeta>) list.get(f.get(null));
// 
//                               /*
//                                * Now we are almost done. This piece of code
//                                * we're in now is called for every biome. Loop
//                                * though the list with BiomeMeta, if the
//                                * BiomeMeta's entity is the one you want to
//                                * change (for example if EntitySkeleton matches
//                                * EntitySkeleton) we will change it to our
//                                * custom entity class
//                                */
//                                for (BiomeMeta meta : metaList) {
//                                	//Get the first field from BiomeMeta.class
//                                    Field clazz = BiomeMeta.class.getDeclaredFields()[0];
//                                    if (clazz.get(meta).equals(nmsClass)) {
//                                        clazz.set(meta, customClass);
//                                    }
//                                }
//                            }
//                        }
// 
//                    }
//                }
//            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	/**
	 * Unregister our entities to prevent memory leaks. Call on disable.
	 */
	public static void unregisterEntities() 
	{
		for (MWEntityType entity : values()) 
		{
			// Remove our class references.
			try 
			{
				((Map) getPrivateStatic(EntityTypes.class, "d")).remove(entity.getCustomClass());
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
 
			try 
			{
				((Map) getPrivateStatic(EntityTypes.class, "f")).remove(entity.getCustomClass());
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
 
		for (MWEntityType entity : values())
		{
			try 
			{
					// Unregister each entity by writing the NMS back in place of the custom class.
					a(entity.getNMSClass(), entity.getName(), entity.getID());
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
			
		//Entities are saved per biome, so access biomes in order to remove entities
		// Biomes#biomes was made private so use reflection to get it.
		BiomeBase[] biomes;
		try 
		{
			biomes = (BiomeBase[]) getPrivateStatic(BiomeBase.class, "biomes");
		}
		catch (Exception exc) 
		{
			// Unable to fetch.
			return;
		}
		
		for (BiomeBase biomeBase : biomes) 
		{
			if (biomeBase == null)
				break;
 
			// The list fields changed names but update the meta regardless.
			//WARNING: Please consider using reflection instead for consistency while accessing NMS code prone to updating names
			//"u", "v", "w", "x" relevant as of Minecraft 1.10.0
			//Find names under BiomeBase.class and search for lists
			//(1.10.0 observations
			//"u" refers to hostile entities found in most biomes
			//"v" refers to non-hostile entities found in most biomes
			//"w" refers to squids found in most biomes
			//"x" refers to bats found in most biomes)
			for (String field : new String[] { /*"as", "at", "au", "av"*/ "u", "v", "w", "x" })
			{
				try 
				{
						Field list = BiomeBase.class.getDeclaredField(field);
						list.setAccessible(true);
						@SuppressWarnings("unchecked")
						List<BiomeMeta> mobList = (List<BiomeMeta>) list.get(biomeBase);
 
						// Make sure the NMS class is written back over our custom class.
						for (BiomeMeta meta : mobList)
						{
							for (MWEntityType entity : values())
							{
								if (entity.getCustomClass().equals(meta.b))
								{
									meta.b = entity.getNMSClass();
								}
							}
						}
				}
				catch (Exception e) 
				{
				e.printStackTrace();
				}
			}
		}
	}
 
	/**
	 * A convenience method.
	 * @param clazz The class to access for private static field
	 * @param f The string representation (name) of the private static field.
	 * @return The object found
	 * @throws Exception if unable to get the object.
	 */
	private static Object getPrivateStatic(Class clazz, String f) throws Exception 
	{
		Field field = clazz.getDeclaredField(f);
		field.setAccessible(true);
		return field.get(null);
	}
 
	/*
	 * Since 1.7.2 added a check in their entity registration, simply bypass it and write to the maps ourself.
	 */
	private static void a(Class<?> paramClass, String paramString, int paramInt) 
	{
		
		try 
		{
			//Removed "c" "e" and "g" to allow default mob spawning and custom mob spawning
			//Snippet from: https://bukkit.org/threads/how-to-make-custom-nms-effect-spawned-in-mobs-not-natural-mobs.337007/#post-2990150
			//((Map) getPrivateStatic(EntityTypes.class, "c")).put(paramString, paramClass);
			((Map) getPrivateStatic(EntityTypes.class, "d")).put(paramClass, paramString);
			//((Map) getPrivateStatic(EntityTypes.class, "e")).put(Integer.valueOf(paramInt), paramClass);
			((Map) getPrivateStatic(EntityTypes.class, "f")).put(paramClass, Integer.valueOf(paramInt));
			//((Map) getPrivateStatic(EntityTypes.class, "g")).put(paramString, Integer.valueOf(paramInt));
		}
		catch (Exception exc) 
		{
			// Unable to register the new class.
		}
	}
}
