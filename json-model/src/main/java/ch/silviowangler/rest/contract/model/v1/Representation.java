/**
 * MIT License
 * <p>
 * Copyright (c) 2016 - 2019 Silvio Wangler (silvio.wangler@gmail.com)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ch.silviowangler.rest.contract.model.v1;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Silvio Wangler
 */
public class Representation implements Serializable {

    private String name;
    private String comment;
    private String responseExample;
    private boolean standard;
    private String mimetype;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getResponseExample() {
        return responseExample;
    }

    public void setResponseExample(String responseExample) {
        this.responseExample = responseExample;
    }

    public boolean isStandard() {
        return standard;
    }

    public void setStandard(boolean standard) {
        this.standard = standard;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public boolean isJson() {
        return getName() != null && getName().equals("json");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Representation that = (Representation) o;
        return standard == that.standard &&
                Objects.equals(name, that.name) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(responseExample, that.responseExample) &&
                Objects.equals(mimetype, that.mimetype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, comment, responseExample, standard, mimetype);
    }

    public static Representation json() {
        Representation representation = new Representation();
        representation.setMimetype("application/json");
        representation.setName("json");
        return representation;
    }
}
