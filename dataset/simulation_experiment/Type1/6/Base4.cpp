#include <stdio.h>

int main(){
    int i;
    int n;
float a;
	double sum; 
	sum=0;
    scanf("%d",&n);
for(i=0;i<n-1;i=i+1){
        scanf("%lf",&a);
        if(a<100){
			a=0;
		}else if(a>=100 && a<200){
a=0.3*a;
		}else if(a>=200 && a<500){
			a=0.3*a;
}else if(a>500){
			a=0.5*a;
		}
        sum=sum+a;
    }
printf("%.2lf",sum);
	return 0;
}

