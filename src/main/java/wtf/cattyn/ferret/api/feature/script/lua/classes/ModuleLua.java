package wtf.cattyn.ferret.api.feature.script.lua.classes;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import wtf.cattyn.ferret.api.feature.module.Module;
import wtf.cattyn.ferret.api.feature.script.Script;
import wtf.cattyn.ferret.api.feature.script.lua.LuaCallback;
import wtf.cattyn.ferret.api.feature.script.lua.utils.LuaUtils;

public class ModuleLua extends Module {

    private final Script script;
    private static LuaValue value;

    private LuaClosure onEnable;
    private LuaClosure onDisable;

    public ModuleLua(String name, String desc, Category category, Script script) {
        super(name, desc, category);
        this.script = script;
    }

    public ModuleLua(String name, String desc, String category, Script script) {
        super(name, desc, Category.valueOf(category));
        this.script = script;
    }

    public Script getScript() {
        return script;
    }

    public void body(LuaClosure luaClosure) {
        LuaUtils.safeCall(script, luaClosure);
        register();
    }

    public LuaCallback registerCallback(String name, LuaClosure luaFunction) {
        LuaCallback callback = new LuaCallback(name, luaFunction, this);
        script.getCallbacks().add(callback);
        return callback;
    }

    public void invoke(String name, LuaValue arg) {
        if(script.getCallbacks() == null) return;
        script.getCallbacks().stream().filter(c -> c.name().equalsIgnoreCase(name)).forEach(c -> c.run(arg));
    }

    public void register() {
        script.addModule(this);
    }

    public void onEnable(LuaClosure closure) {
        this.onEnable = closure;
    }

    public void onDisable(LuaClosure closure) {
        this.onDisable = closure;
    }

    @Override public void onEnable() {
        LuaUtils.safeCall(onEnable);
    }

    @Override public void onDisable() {
        LuaUtils.safeCall(onDisable);
    }

    public static LuaValue getLua() {
        if(value == null) {
            LuaValue moduleLua = CoerceJavaToLua.coerce(ModuleLua.class);
            LuaTable table = new LuaTable();
            table.set("new", new New());
            table.set("__index", table);
            moduleLua.setmetatable(table);
            value = moduleLua;
        }
        return value;
    }

    @Override public String toString() {
        return "ModuleLua{" +
                "script=" + script +
                "} " + super.toString();
    }

    static class New extends LibFunction {

        public LuaValue call(LuaValue name, LuaValue description, LuaValue category, LuaValue script) {
            if(!name.isstring() || !description.isstring() || !category.isstring()) throw new IllegalArgumentException("Invalid arguments.");
            return CoerceJavaToLua.coerce(
                    new ModuleLua(name.tojstring(), description.tojstring(), Category.valueOf(category.tojstring()), ( Script ) CoerceLuaToJava.coerce(script, Script.class))
            );
        }

    }

}
