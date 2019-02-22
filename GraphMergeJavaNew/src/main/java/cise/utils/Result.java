package cise.utils;

/**
 * 用于函数返回类型
 */
public final class Result {

    public boolean flag;
    public String message;

    private Result(boolean flag, String message) {
        this.flag = flag;
        this.message = message;
    }

    public static Result success() {
        return new Result(true, "");
    }

    public static Result failed() {
        return failed("");
    }

    public static Result failed(String msg) {
        return new Result(false, msg);
    }

    public boolean isSuccess() {
        return this.flag;
    }
}


