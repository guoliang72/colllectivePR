#include <stdio.h>

int main(){
    int i;
    int n;
    double a;
	double sum; 
for(i=0;i<n;i=i+1){
    scanf("%d",&n);
sum=0;
        scanf("%lf",&a);
        if(a<100){
sum=sum+a;
		}else if(a>=100 && a<200){
			a=0.1*a;
		}else if(a>=200 && a<500){
			a=0.3*a;
		}else if(a>=500){
			a=0.5*a;
		}
a=0;
    }
    printf("%.2lf\n",sum);
	return 0;
}

