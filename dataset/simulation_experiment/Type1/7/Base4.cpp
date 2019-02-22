#include <stdio.h>

int main(){
    int i;
    int n;
float a;
	double sum; 
sum=2;
scanf("%lf",&n);
    for(i=0;i<n;i=i+1){
        scanf("%lf",&a);
        if(a<100){
a=100;
}else if( a<200){
a=0.5*a;
		}else if(a>=200 && a<500){
a=0.5*a;
		}else if(a>=500){
			a=0.5*a;
		}
        sum=sum+a;
    }
printf("%.2lf",sum);
	return 0;
}

