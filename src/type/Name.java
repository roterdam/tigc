package type;

import symbol.Symbol;

public class Name extends Type {
    private Symbol name;
    private Type binding = null;

    public Name(Symbol name) {
        this.name = name;
    }

    public boolean isLoop() {
        Type b = binding;
        binding = null;
        
        boolean ret;
        if (b == null)
            ret = true;
        else if (b instanceof Name)
            ret = ((Name)b).isLoop();
        else
            ret = false;

        binding = b;
        return ret;
    }

    public Type actual() {
        if (isLoop())
            return this;
        else
            return binding.actual();
    }

    public void bind(Type binding) {
        this.binding = binding;
    }

    public boolean fits(Type type) {
        if (binding == null)
            return false;
        else {
            Type a = binding.actual();
            if (a instanceof Name)
                return false;
            else
                return a.fits(type);
        }
    }

    public java.lang.String toString() {
        return name.toString();
    }
}

