package transfer.exception;

/**
 * 编译错误
 * Created by Jake on 2015/3/8.
 */
public class CompileError extends RuntimeException {

    public CompileError(Exception e) {
        super("asm 模版编译错误 !", e);
    }

	public CompileError(String arg0) {
		super(arg0);
	}
	

}
