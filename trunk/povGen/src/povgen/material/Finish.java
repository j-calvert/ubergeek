package povgen.material;

public enum Finish {

	Chrome, Metal, Polished, Brass, Silver, Soft_Silver, Metallic;

	@Override
	public String toString() {
//		return "finish{" + name() + "_Finish}";
	//	return "";//finish{Metal_Finish}";
		if(this == Polished) {
			return "finish{Polished_Chrome_Finish}";			
		} else {
			return "finish{Chrome_Finish}";
		}
	}
}
