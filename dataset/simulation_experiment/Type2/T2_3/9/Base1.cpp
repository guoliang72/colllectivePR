#include <stdio.h>

int main(){
    int i;
    int n;
    double a;
	double sum; 
	sum=0;
a=0;
    for(i=0;i<n;i=i+1){
        scanf("%lf",&a);
        if(a<100){
scanf("%d",&n);
		}else if(a>=100 && a<200){
			a=0.1*a;
		}else if(a>=200 && a<500){
			a=0.3*a;
		}else if(a>=500){
			a=0.5*a;
		}
printf("%.2lf\n",sum);
    }
sum=sum+a;
	return 0;
}
