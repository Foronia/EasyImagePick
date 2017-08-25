# EasyImagePick
简单易用的图片选择器

## 使用
1. 继承ImageUploadAcyivity
2. 将onCreate替换为initContentView  
  
    @Override    
    
    protected void initContentView(Bundle savedInstanceState) {  
    
        super.initContentView(savedInstanceState);  
	
        setChildContentView(R.layout.activity_main);  
	
    }

### 就这么简单！  
  
    
    

tips：可选择图片数量默认为9，如需更改，可重写set_datas进行设置：  
     
    @Override  
    
    protected void set_datas(){  
    
        super.set_datas();  
	
        setLimit(3);//若不指定，默认为9  
	
    }

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

