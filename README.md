# EasyImagePick
简单易用的图片选择器

## 使用
<p>1.继承ImageUploadAcyivity</p>
<p>2.调用setLimit(int limit)设置可选择图片上限（当然你也可以不设置，默认为9）</p>

### 就这么简单！

## 准备

#### Project build.gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

#### module build.gradle
  	dependencies {
	        compile 'com.github.Foronia:EasyImagePick:-SNAPSHOT'
	}

