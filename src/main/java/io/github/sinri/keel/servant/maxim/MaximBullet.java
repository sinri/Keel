package io.github.sinri.keel.servant.maxim;

import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.vertx.core.Future;

/**
 * 集群模式下零散任务的参数和执行器抽象。
 *
 * @since 2.9.1
 */
abstract public class MaximBullet extends SimpleJsonifiableEntity {

    public MaximBullet() {
        super();
        this.jsonObject.put("id", null);
    }

    public String getID() {
        return this.readString("id");
    }

    public MaximBullet setID(String id) {
        this.jsonObject.put("id", id);
        return this;
    }

    public MaximBullet setImplement(Class<? extends MaximBullet> implementClass) {
        this.jsonObject.put("implement", implementClass.getName());
        return this;
    }

    public Class<?> getImplementClass() throws ClassNotFoundException {
        String implement = this.readString("implement");
        return Class.forName(implement);
    }

    abstract public Future<Void> fire();


}
